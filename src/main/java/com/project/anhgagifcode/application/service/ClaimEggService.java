package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.ClaimEggUseCase;
import com.project.anhgagifcode.application.port.in.SyncKiotvietOrderUseCase;
import com.project.anhgagifcode.application.port.in.dto.ClaimEggResponse;
import com.project.anhgagifcode.application.port.in.dto.ClaimedAccountDto;
import com.project.anhgagifcode.application.port.out.*;
import com.project.anhgagifcode.domain.exception.BusinessRuleViolationException;
import com.project.anhgagifcode.domain.exception.ResourceNotFoundException;
import com.project.anhgagifcode.domain.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class ClaimEggService implements ClaimEggUseCase {

    private final EggPersistencePort eggPort;
    private final KiotvietOrderPersistencePort orderPort;
    private final CustomerPersistencePort customerPort;
    private final GiftAccountPersistencePort accountPort;
    private final EggOpeningLogPersistencePort logPort;
    private final SyncKiotvietOrderUseCase syncOrderUseCase;
    private final NotificationPort notificationPort;
    private final TransactionTemplate transactionTemplate;

    public ClaimEggService(
            EggPersistencePort eggPort,
            KiotvietOrderPersistencePort orderPort,
            CustomerPersistencePort customerPort,
            GiftAccountPersistencePort accountPort,
            EggOpeningLogPersistencePort logPort,
            SyncKiotvietOrderUseCase syncOrderUseCase,
            NotificationPort notificationPort,
            PlatformTransactionManager transactionManager) {
        this.eggPort = eggPort;
        this.orderPort = orderPort;
        this.customerPort = customerPort;
        this.accountPort = accountPort;
        this.logPort = logPort;
        this.syncOrderUseCase = syncOrderUseCase;
        this.notificationPort = notificationPort;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public ClaimEggResponse claimEggReward(String orderId, int eggType, String ipAddress) {
        // 1. Tải thông tin trứng trong nhóm (Read-Only) để lấy đơn hàng
        List<Egg> initialEggs = eggPort.loadEggsForClaimReadOnly(orderId, eggType);
        if (initialEggs.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy trứng hợp lệ cho đơn hàng và sản phẩm này.");
        }

        Egg firstEgg = initialEggs.get(0);

        // Nếu tất cả trứng trong nhóm đã được mở trước đó, trả về danh sách tài khoản ngay lập tức
        boolean allInitiallyClaimed = initialEggs.stream().allMatch(e -> "CLAIMED".equals(e.getStatus()));
        if (allInitiallyClaimed) {
            List<ClaimedAccountDto> claimedAccounts = new ArrayList<>();
            for (Egg egg : initialEggs) {
                GiftAccount assignedAccount = egg.getAccount();
                if (assignedAccount != null) {
                    claimedAccounts.add(ClaimedAccountDto.builder()
                            .username(assignedAccount.getUsername())
                            .password(assignedAccount.getPassword())
                            .platform(assignedAccount.getPlatform())
                            .tier(egg.getGiftPool() != null ? egg.getGiftPool().getTier() : null)
                            .build());
                }
            }
            return ClaimEggResponse.builder()
                    .accounts(claimedAccounts)
                    .stuckCount(0)
                    .message("Dưới đây là danh sách thông tin tài khoản đã nhận của bạn.")
                    .build();
        }

        boolean anyCancelled = initialEggs.stream().anyMatch(e -> "CANCELLED".equals(e.getStatus()));
        if (anyCancelled) {
            throw new BusinessRuleViolationException("Nhóm trứng này đã bị hủy.");
        }

        // 2. Đồng bộ trạng thái đơn hàng thời gian thực ngoài Transaction nếu quá hạn 5 phút cache
        KiotvietOrder syncedOrder = syncOrderUseCase.syncOrderIfNeeded(firstEgg.getOrder());

        // 3. Thực hiện bốc quà và cập nhật trạng thái trong Transaction
        return transactionTemplate.execute(status -> {
            // 3.1 Load và Khóa danh sách Trứng (Lock For Update)
            List<Egg> eggsToClaim = eggPort.loadEggsForClaim(orderId, eggType);
            if (eggsToClaim.isEmpty()) {
                throw new ResourceNotFoundException("Không tìm thấy trứng hợp lệ cho đơn hàng và sản phẩm này.");
            }

            // Nếu tất cả trứng đã được mở trong lúc đồng bộ, trả về danh sách tài khoản
            boolean allClaimed = eggsToClaim.stream().allMatch(e -> "CLAIMED".equals(e.getStatus()));
            if (allClaimed) {
                List<ClaimedAccountDto> claimedAccounts = new ArrayList<>();
                for (Egg egg : eggsToClaim) {
                    GiftAccount assignedAccount = egg.getAccount();
                    if (assignedAccount != null) {
                        claimedAccounts.add(ClaimedAccountDto.builder()
                                .username(assignedAccount.getUsername())
                                .password(assignedAccount.getPassword())
                                .platform(assignedAccount.getPlatform())
                                .tier(egg.getGiftPool() != null ? egg.getGiftPool().getTier() : null)
                                .build());
                    }
                }
                return ClaimEggResponse.builder()
                        .accounts(claimedAccounts)
                        .stuckCount(0)
                        .message("Dưới đây là danh sách thông tin tài khoản đã nhận của bạn.")
                        .build();
            }

            boolean hasCancelled = eggsToClaim.stream().anyMatch(e -> "CANCELLED".equals(e.getStatus()));
            if (hasCancelled) {
                throw new BusinessRuleViolationException("Nhóm trứng này đã bị hủy.");
            }

            // Đảm bảo trứng tham chiếu tới order đã được đồng bộ mới nhất
            for (Egg egg : eggsToClaim) {
                egg.setOrder(syncedOrder);
            }

            // 3.2 Kiểm tra đơn hàng phải giao thành công mới được mở trứng
            String deliveryStatus = syncedOrder.getDeliveryStatus();
            boolean isReturnedOrCancelled = "Đang chuyển hoàn".equalsIgnoreCase(deliveryStatus) || "Đã chuyển hoàn".equalsIgnoreCase(deliveryStatus)
                    || "Hủy".equalsIgnoreCase(deliveryStatus) || "Đã hủy".equalsIgnoreCase(deliveryStatus) || "Bị hủy".equalsIgnoreCase(deliveryStatus);
            if (isReturnedOrCancelled) {
                for (Egg egg : eggsToClaim) {
                    if (!"CANCELLED".equals(egg.getStatus())) {
                        egg.setStatus("CANCELLED");
                        eggPort.saveEgg(egg);
                    }
                }
                
                Customer customer = customerPort.loadByCustomerCodeForUpdate(syncedOrder.getCustomerCode())
                        .orElseThrow(() -> new ResourceNotFoundException("Lỗi dữ liệu khách hàng."));
                customer.setEarlyHatchCredits(0);
                customerPort.saveCustomer(customer);
                
                throw new BusinessRuleViolationException("Đơn hàng đã bị hoàn trả hoặc hủy, nhóm trứng này đã bị hủy.");
            }

            boolean isDelivered = "Đã giao hàng".equalsIgnoreCase(deliveryStatus) || "Giao thành công".equalsIgnoreCase(deliveryStatus);
            if (!isDelivered) {
                throw new BusinessRuleViolationException("Đơn hàng chưa được giao thành công.");
            }

            // 3.3 Load thông tin khách hàng mới nhất có khóa bi quan
            Customer customer = customerPort.loadByCustomerCodeForUpdate(syncedOrder.getCustomerCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Lỗi dữ liệu khách hàng."));

            // 3.4 Kiểm tra trạng thái cấm (BANNED)
            if ("BANNED".equals(customer.getStatus())) {
                throw new BusinessRuleViolationException("Tài khoản bị khóa do vi phạm chính sách");
            }

            // 3.5 Cập nhật trạng thái trứng động dựa trên thông tin thực tế mới nhất trước khi kiểm tra
            for (Egg egg : eggsToClaim) {
                if (!"CLAIMED".equals(egg.getStatus()) && !"CANCELLED".equals(egg.getStatus())) {
                    String dynamicStatus;
                    if (!isDelivered) {
                        dynamicStatus = "WAITING_ORDER_COMPLETION";
                    } else {
                        boolean inHatchCooldown = egg.getHatchAt() != null && LocalDateTime.now().isBefore(egg.getHatchAt());
                        if (inHatchCooldown) {
                            dynamicStatus = "HATCHING";
                        } else {
                            dynamicStatus = "READY_TO_CLAIM";
                        }
                    }

                    if (!dynamicStatus.equals(egg.getStatus())) {
                        egg.setStatus(dynamicStatus);
                        eggPort.saveEgg(egg);
                    }
                }
            }

            // 3.6 Kiểm tra trạng thái trứng sau khi cập nhật động
            List<Egg> readyEggs = eggsToClaim.stream()
                    .filter(e -> "READY_TO_CLAIM".equals(e.getStatus()))
                    .collect(Collectors.toList());

            if (readyEggs.isEmpty()) {
                boolean hasHatching = eggsToClaim.stream().anyMatch(e -> "HATCHING".equals(e.getStatus()));
                if (hasHatching) {
                    throw new BusinessRuleViolationException("Trứng đang ấp, chưa đến thời gian mở.");
                }
                throw new BusinessRuleViolationException("Không có trứng nào sẵn sàng để nhận.");
            }

            // 3.8 Bốc Quà & Partial Success
            List<ClaimedAccountDto> claimedAccounts = new ArrayList<>();
            // Thu thập các tài khoản đã được claim trước đó của nhóm này (nếu có)
            for (Egg egg : eggsToClaim) {
                if ("CLAIMED".equals(egg.getStatus()) && egg.getAccount() != null) {
                    claimedAccounts.add(ClaimedAccountDto.builder()
                            .username(egg.getAccount().getUsername())
                            .password(egg.getAccount().getPassword())
                            .platform(egg.getAccount().getPlatform())
                            .tier(egg.getGiftPool() != null ? egg.getGiftPool().getTier() : null)
                            .build());
                }
            }

            int stuckCount = 0;
            List<Egg> updatedEggs = new ArrayList<>();

            for (Egg egg : readyEggs) {
                String poolId = egg.getGiftPool().getId();
                Optional<GiftAccount> accountOpt = accountPort.pickAvailableAccountForUpdateSkipLocked(poolId);

                if (accountOpt.isPresent()) {
                    GiftAccount assignedAccount = accountOpt.get();
                    assignedAccount.setStatus("ASSIGNED");
                    assignedAccount.setAssignedAt(LocalDateTime.now());
                    accountPort.updateAccount(assignedAccount);

                    egg.setStatus("CLAIMED");
                    egg.setAccount(assignedAccount);
                    updatedEggs.add(egg);

                    // 3.11 Ghi Log hệ thống
                    EggOpeningLog logEntry = EggOpeningLog.builder()
                            .id(UUID.randomUUID().toString())
                            .eggId(egg.getId())
                            .accountId(assignedAccount.getId())
                            .actionType("CLAIM_REWARD")
                            .triggeredBy("USER_IP")
                            .ipAddress(ipAddress)
                            .createdAt(LocalDateTime.now())
                            .build();
                    logPort.saveLog(logEntry);

                    claimedAccounts.add(ClaimedAccountDto.builder()
                            .username(assignedAccount.getUsername())
                            .password(assignedAccount.getPassword())
                            .platform(assignedAccount.getPlatform())
                            .tier(egg.getGiftPool() != null ? egg.getGiftPool().getTier() : null)
                            .build());
                } else {
                    notificationPort.sendAlert(String.format(
                            "🚨 <b>CẢNH BÁO SỰ CỐ: Hết quà tặng khả dụng</b>\n" +
                            "• Trứng ID: <code>%s</code>\n" +
                            "• Loại trứng: <code>Loại %d</code>\n" +
                            "• Bể quà: <code>%s</code> (ID: <code>%s</code>)\n" +
                            "• Lỗi: Không còn tài khoản khả dụng (AVAILABLE) để phát thưởng.",
                            egg.getId(), egg.getEggType(),
                            egg.getGiftPool() != null ? egg.getGiftPool().getPoolName() : "Không rõ", poolId
                    ));
                    stuckCount++;
                }
            }

            if (!updatedEggs.isEmpty()) {
                eggPort.saveAllEggs(updatedEggs);
            }

            // 3.12 Xử lý điều kiện Ân xá (Reset Streak) cho khách hàng WARNING
            if (customer.getReturnStreak() == 1) {
                processAmnesty(customer, syncedOrder);
            }

            // TÍCH LŨY TÍN DỤNG DUYỆT SỚM (Chỉ khi mở thành công tất cả trứng của đơn và là trứng loại 2)
            List<Egg> allOrderEggs = eggPort.loadEggsByOrderId(syncedOrder.getId());
            boolean allClaimedInOrder = allOrderEggs.stream()
                    .allMatch(e -> "CLAIMED".equalsIgnoreCase(e.getStatus()));

            if (allClaimedInOrder && eggType == 2 && customer.getReturnCount() == 0 && !"BANNED".equals(customer.getStatus())) {
                customer.setEarlyHatchCredits(2);
            }
            customerPort.saveCustomer(customer);

            String msg = stuckCount > 0 
                ? String.format("Mở trứng hoàn tất. Thành công: %d, Kẹt (thiếu tài khoản VIP): %d.", updatedEggs.size(), stuckCount)
                : "Chúc mừng! Bạn đã mở trứng thành công.";

            return ClaimEggResponse.builder()
                    .accounts(claimedAccounts)
                    .stuckCount(stuckCount)
                    .message(msg)
                    .build();
        });
    }

    private boolean isAbsoluteSuccess(KiotvietOrder order) {
        if (!"Đã giao hàng".equalsIgnoreCase(order.getDeliveryStatus())) {
            return false;
        }
        LocalDateTime deliveryDate = order.getUpdatedAt() != null ? order.getUpdatedAt() : order.getCreatedAt();
        return deliveryDate.plusDays(15).isBefore(LocalDateTime.now());
    }

    private void processAmnesty(Customer customer, KiotvietOrder currentOrder) {
        // Load all orders of this customer
        List<KiotvietOrder> customerOrders = orderPort.findByCustomerCode(customer.getCustomerCode());

        // Find creation timestamp of the latest returned order
        LocalDateTime latestReturnTime = customerOrders.stream()
                .filter(o -> "Đang chuyển hoàn".equalsIgnoreCase(o.getDeliveryStatus()) 
                        || "Đã chuyển hoàn".equalsIgnoreCase(o.getDeliveryStatus()))
                .map(KiotvietOrder::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.MIN);

        // Get orders created after the latest returned order
        List<KiotvietOrder> postReturnOrders = customerOrders.stream()
                .filter(o -> o.getCreatedAt().isAfter(latestReturnTime))
                .collect(Collectors.toList());

        int fullySuccessfulOrders = 0;
        for (KiotvietOrder postOrder : postReturnOrders) {
            // Check if order is in absolute success state
            if (isAbsoluteSuccess(postOrder)) {
                List<Egg> postOrderEggs = eggPort.loadEggsByOrderId(postOrder.getId());
                if (!postOrderEggs.isEmpty()) {
                    // Check if all eggs have been successfully claimed/opened
                    boolean allClaimed = postOrderEggs.stream()
                            .allMatch(e -> "CLAIMED".equalsIgnoreCase(e.getStatus()));
                    if (allClaimed) {
                        fullySuccessfulOrders++;
                    }
                }
            }
        }

        // Reset streak if customer has successfully opened all eggs of at least 2 orders post-return
        if (fullySuccessfulOrders >= 2) {
            customer.setReturnStreak(0);
            if (customer.getSuccessCount() >= 5) {
                customer.setStatus("TRUSTED_2");
            } else if (customer.getSuccessCount() >= 2) {
                customer.setStatus("TRUSTED_1");
            } else {
                customer.setStatus("NEW");
            }
            customerPort.saveCustomer(customer);
            log.info("Khách hàng {} được ân xá, reset return streak về 0.", customer.getCustomerCode());
        }
    }
}