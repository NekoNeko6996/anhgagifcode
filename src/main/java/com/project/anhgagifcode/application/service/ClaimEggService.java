package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.ClaimEggUseCase;
import com.project.anhgagifcode.application.port.in.SyncKiotvietOrderUseCase;
import com.project.anhgagifcode.application.port.in.dto.ClaimEggResponse;
import com.project.anhgagifcode.application.port.out.*;
import com.project.anhgagifcode.domain.exception.BusinessRuleViolationException;
import com.project.anhgagifcode.domain.exception.ResourceNotFoundException;
import com.project.anhgagifcode.domain.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
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
    public ClaimEggResponse claimEggReward(String eggId, String ipAddress) {
        // 1. Tải thông tin trứng không khóa (Read-Only) để lấy đơn hàng
        Egg initialEgg = eggPort.findById(eggId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trứng hợp lệ."));

        // Nếu trứng đã được mở trước đó, trả về thông tin tài khoản VIP ngay lập tức mà không cần đồng bộ hóa đơn hàng
        if ("CLAIMED".equals(initialEgg.getStatus())) {
            GiftAccount assignedAccount = initialEgg.getAccount();
            if (assignedAccount != null) {
                return ClaimEggResponse.builder()
                        .username(assignedAccount.getUsername())
                        .password(assignedAccount.getPassword())
                        .platform(assignedAccount.getPlatform())
                        .tier(initialEgg.getGiftPool() != null ? initialEgg.getGiftPool().getTier() : null)
                        .message("Dưới đây là thông tin tài khoản đã nhận của bạn.")
                        .build();
            } else {
                throw new BusinessRuleViolationException("Trứng đã mở nhưng không tìm thấy thông tin quà tặng.");
            }
        }
        if ("CANCELLED".equals(initialEgg.getStatus())) {
            throw new BusinessRuleViolationException("Trứng này đã bị hủy.");
        }

        // 2. Đồng bộ trạng thái đơn hàng thời gian thực ngoài Transaction nếu quá hạn 5 phút cache
        KiotvietOrder syncedOrder = syncOrderUseCase.syncOrderIfNeeded(initialEgg.getOrder());

        // 3. Thực hiện bốc quà và cập nhật trạng thái trong Transaction
        return transactionTemplate.execute(status -> {
            // 3.1 Load và Khóa Trứng (Lock For Update)
            Egg egg = eggPort.loadEggForUpdate(eggId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trứng hợp lệ."));

            // Nếu trứng đã được mở trong lúc đồng bộ, trả về thông tin tài khoản VIP
            if ("CLAIMED".equals(egg.getStatus())) {
                GiftAccount assignedAccount = egg.getAccount();
                if (assignedAccount != null) {
                    return ClaimEggResponse.builder()
                            .username(assignedAccount.getUsername())
                            .password(assignedAccount.getPassword())
                            .platform(assignedAccount.getPlatform())
                            .tier(egg.getGiftPool() != null ? egg.getGiftPool().getTier() : null)
                            .message("Dưới đây là thông tin tài khoản đã nhận của bạn.")
                            .build();
                } else {
                    throw new BusinessRuleViolationException("Trứng đã mở nhưng không tìm thấy thông tin quà tặng.");
                }
            }
            if ("CANCELLED".equals(egg.getStatus())) {
                throw new BusinessRuleViolationException("Trứng này đã bị hủy.");
            }

            // Đảm bảo trứng tham chiếu tới order đã được đồng bộ mới nhất
            egg.setOrder(syncedOrder);

            // 3.2 Kiểm tra đơn hàng phải giao thành công mới được mở trứng
            String deliveryStatus = syncedOrder.getDeliveryStatus();
            boolean isReturned = "Đang chuyển hoàn".equalsIgnoreCase(deliveryStatus) || "Đã chuyển hoàn".equalsIgnoreCase(deliveryStatus);
            if (isReturned) {
                if (!"CANCELLED".equals(egg.getStatus())) {
                    egg.setStatus("CANCELLED");
                    eggPort.saveEgg(egg);
                }
                throw new BusinessRuleViolationException("Đơn hàng đã bị hoàn trả, trứng này đã bị hủy.");
            }

            boolean isDelivered = "Đã giao hàng".equalsIgnoreCase(deliveryStatus) || "Giao thành công".equalsIgnoreCase(deliveryStatus);
            if (!isDelivered) {
                throw new BusinessRuleViolationException("Đơn hàng chưa được giao thành công.");
            }

            // 3.3 Load thông tin khách hàng mới nhất
            Customer customer = customerPort.loadByCustomerCode(syncedOrder.getCustomerCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Lỗi dữ liệu khách hàng."));

            // 3.4 Kiểm tra trạng thái cấm (BANNED)
            if ("BANNED".equals(customer.getStatus())) {
                throw new BusinessRuleViolationException("Tài khoản bị khóa do vi phạm chính sách");
            }

            // 3.5 Cập nhật trạng thái trứng động dựa trên thông tin thực tế mới nhất trước khi kiểm tra
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

            // 3.6 Kiểm tra trạng thái trứng sau khi cập nhật động
            if ("HATCHING".equals(egg.getStatus())) {
                throw new BusinessRuleViolationException("Trứng đang ấp, chưa đến thời gian mở.");
            }
            if ("WAITING_ORDER_COMPLETION".equals(egg.getStatus())) {
                throw new BusinessRuleViolationException("Đơn hàng chưa đạt trạng thái thành công tuyệt đối hoặc chưa đủ 15 ngày.");
            }
            if (!"READY_TO_CLAIM".equals(egg.getStatus())) {
                throw new BusinessRuleViolationException("Trứng chưa sẵn sàng để nhận.");
            }

            // 3.8 Bốc Quà (Sử dụng native query SKIP LOCKED để khóa và phân bổ cực nhanh, chống deadlock)
            String poolId = egg.getGiftPool().getId();
            GiftAccount assignedAccount = accountPort.pickAvailableAccountForUpdateSkipLocked(poolId)
                    .orElseThrow(() -> {
                        notificationPort.sendAlert(String.format(
                                "🚨 <b>CẢNH BÁO SỰ CỐ: Hết quà tặng khả dụng</b>\n" +
                                "• Trứng ID: <code>%s</code>\n" +
                                "• Loại trứng: <code>Loại %d</code>\n" +
                                "• Bể quà: <code>%s</code> (ID: <code>%s</code>)\n" +
                                "• Lỗi: Không còn tài khoản khả dụng (AVAILABLE) để phát thưởng.",
                                egg.getId(), egg.getEggType(),
                                egg.getGiftPool() != null ? egg.getGiftPool().getPoolName() : "Không rõ", poolId
                        ));
                        return new BusinessRuleViolationException("Kho quà hiện tại đã hết, vui lòng quay lại sau.");
                    });

            // 3.9 Cập nhật trạng thái tài khoản quà tặng
            assignedAccount.setStatus("ASSIGNED");
            assignedAccount.setAssignedAt(LocalDateTime.now());
            accountPort.updateAccount(assignedAccount);

            // 3.10 Cập nhật thông tin trứng
            egg.setStatus("CLAIMED");
            egg.setAccount(assignedAccount);
            eggPort.saveEgg(egg);

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

            // 3.12 Xử lý điều kiện Ân xá (Reset Streak) cho khách hàng WARNING
            if (customer.getReturnStreak() == 1) {
                processAmnesty(customer, syncedOrder);
            }

            return ClaimEggResponse.builder()
                    .username(assignedAccount.getUsername())
                    .password(assignedAccount.getPassword())
                    .platform(assignedAccount.getPlatform())
                    .tier(egg.getGiftPool() != null ? egg.getGiftPool().getTier() : null)
                    .message("Chúc mừng! Bạn đã mở trứng thành công.")
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