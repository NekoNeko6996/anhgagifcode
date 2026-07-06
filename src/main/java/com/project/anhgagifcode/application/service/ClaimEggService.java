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
            return buildResponseFromClaimedEggs(initialEggs, 0, "Dưới đây là danh sách thông tin tài khoản đã nhận của bạn.");
        }

        boolean anyCancelled = initialEggs.stream().anyMatch(e -> "CANCELLED".equals(e.getStatus()));
        if (anyCancelled) {
            throw new BusinessRuleViolationException("Nhóm trứng này đã bị hủy.");
        }

        // 2. Đồng bộ trạng thái đơn hàng thời gian thực ngoài Transaction nếu quá hạn 5 phút cache
        // Hàm này SẼ TỰ ĐỘNG phạt KH và hủy trứng nếu KiotViet báo đơn bị hoàn/hủy
        KiotvietOrder syncedOrder = syncOrderUseCase.syncOrderIfNeeded(firstEgg.getOrder());

        // 3. Thực hiện bốc quà và cập nhật trạng thái trong Transaction
        return transactionTemplate.execute(status -> {
            // 3.1 Load và Khóa danh sách Trứng (Lock For Update)
            List<Egg> eggsToClaim = eggPort.loadEggsForClaim(orderId, eggType);
            if (eggsToClaim.isEmpty()) {
                throw new ResourceNotFoundException("Không tìm thấy trứng hợp lệ cho đơn hàng và sản phẩm này.");
            }

            // Ghi nhận trạng thái hoàn thành của toàn bộ đơn hàng TRƯỚC KHI xử lý trứng hiện tại
            List<Egg> allOrderEggsBefore = eggPort.loadEggsByOrderId(syncedOrder.getId());
            boolean wasAlreadyFullyClaimed = allOrderEggsBefore.stream()
                    .allMatch(e -> "CLAIMED".equalsIgnoreCase(e.getStatus()));

            // Nếu tất cả trứng đã được mở trong lúc đồng bộ, trả về danh sách tài khoản
            boolean allClaimed = eggsToClaim.stream().allMatch(e -> "CLAIMED".equals(e.getStatus()));
            if (allClaimed) {
                return buildResponseFromClaimedEggs(eggsToClaim, 0, "Dưới đây là danh sách thông tin tài khoản đã nhận của bạn.");
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
                // Logic phạt và hủy trứng đã được syncOrderUseCase lo liệu, ở đây chỉ cần chặn request đi tiếp
                throw new BusinessRuleViolationException("Đơn hàng đã bị hoàn trả hoặc hủy, nhóm trứng này đã bị hủy.");
            }

            boolean isDelivered = "Đã giao hàng".equalsIgnoreCase(deliveryStatus) || "Giao thành công".equalsIgnoreCase(deliveryStatus);
            if (!isDelivered) {
                throw new BusinessRuleViolationException("Đơn hàng chưa được giao thành công.");
            }

            // 3.3 Load thông tin khách hàng mới nhất có khóa bi quan
            Customer customer = customerPort.loadByCustomerCodeForUpdate(syncedOrder.getCustomerCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Lỗi dữ liệu khách hàng."));

            // 3.4 Kiểm tra trạng thái cấm (BANNED / TEMP_BANNED)
            if ("BANNED".equalsIgnoreCase(customer.getStatus())) {
                throw new BusinessRuleViolationException("Tài khoản bị khóa do vi phạm chính sách");
            }
            if ("TEMP_BANNED".equalsIgnoreCase(customer.getStatus()) && customer.getUnbanAt() != null) {
                if (LocalDateTime.now().isBefore(customer.getUnbanAt())) {
                    throw new BusinessRuleViolationException("Tài khoản của bạn đang bị khóa tạm thời đến ngày " + customer.getUnbanAt() + ".");
                }
                // Nếu đã qua thời hạn (Lazy Unban): Cho phép đi tiếp mở trứng, nhưng chưa được gỡ thẻ phạt
            }

            // 3.5 Cập nhật trạng thái trứng động
            for (Egg egg : eggsToClaim) {
                if (!"CLAIMED".equals(egg.getStatus()) && !"CANCELLED".equals(egg.getStatus())) {
                    String dynamicStatus;
                    boolean inHatchCooldown = egg.getHatchAt() != null && LocalDateTime.now().isBefore(egg.getHatchAt());
                    if (inHatchCooldown) {
                        dynamicStatus = "HATCHING";
                    } else {
                        dynamicStatus = "READY_TO_CLAIM";
                    }

                    if (!dynamicStatus.equals(egg.getStatus())) {
                        egg.setStatus(dynamicStatus);
                        eggPort.saveEgg(egg);
                    }
                }
            }

            // 3.6 Kiểm tra trứng đã sẵn sàng
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

            // 3.8 Bốc Quà
            List<ClaimedAccountDto> claimedAccounts = eggsToClaim.stream()
                    .filter(e -> "CLAIMED".equals(e.getStatus()) && e.getAccount() != null)
                    .map(e -> ClaimedAccountDto.builder()
                            .username(e.getAccount().getUsername())
                            .password(e.getAccount().getPassword())
                            .platform(e.getAccount().getPlatform())
                            .tier(e.getGiftPool() != null ? e.getGiftPool().getTier() : null)
                            .build())
                    .collect(Collectors.toList());

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

            // 3.12 XÉT ĐIỀU KIỆN CHUỘC LỖI & CỘNG VIP (Mở xong toàn bộ đơn hàng)
            if (!wasAlreadyFullyClaimed) {
                List<Egg> allOrderEggsAfter = eggPort.loadEggsByOrderId(syncedOrder.getId());
                boolean nowFullyClaimed = allOrderEggsAfter.stream()
                        .allMatch(e -> "CLAIMED".equalsIgnoreCase(e.getStatus()));

                if (nowFullyClaimed) {
                    // A. Chỉ cộng success_count nếu đơn hàng có TRỨNG VIP
                    boolean containsVipEgg = allOrderEggsAfter.stream().anyMatch(e -> e.getEggType() == 2);
                    if (containsVipEgg) {
                        customer.setSuccessCount(customer.getSuccessCount() + 1);
                    }

                    // B. Hạ cấp phạt (Giảm án)
                    if ("TEMP_BANNED".equalsIgnoreCase(customer.getStatus())) {
                        customer.setStatus("WARNING");
                        customer.setReturnStreak(1);
                        customer.setUnbanAt(null);
                    } else if ("WARNING".equalsIgnoreCase(customer.getStatus())) {
                        customer.setStatus("NORMAL");
                        customer.setReturnStreak(0);
                    }
                    
                    // C. Tích lũy tín dụng duyệt sớm (Early Hatch Credits)
                    // (Chỉ khi trứng vừa mở là trứng loại 2 và khách chưa từng có lịch sử hoàn hàng)
                    if (containsVipEgg && customer.getReturnCount() == 0 && !"BANNED".equalsIgnoreCase(customer.getStatus())) {
                        customer.setEarlyHatchCredits(2);
                    }
                }
            }
            
            customerPort.saveCustomer(customer);

            String msg = stuckCount > 0 
                ? String.format("Mở trứng hoàn tất.", updatedEggs.size(), stuckCount)
                : "Chúc mừng! Bạn đã mở trứng thành công.";

            return ClaimEggResponse.builder()
                    .accounts(claimedAccounts)
                    .stuckCount(stuckCount)
                    .message(msg)
                    .build();
        });
    }

    // Hàm tiện ích để giảm bớt code thừa
    private ClaimEggResponse buildResponseFromClaimedEggs(List<Egg> eggs, int stuckCount, String message) {
        List<ClaimedAccountDto> claimedAccounts = eggs.stream()
                .filter(e -> e.getAccount() != null)
                .map(e -> ClaimedAccountDto.builder()
                        .username(e.getAccount().getUsername())
                        .password(e.getAccount().getPassword())
                        .platform(e.getAccount().getPlatform())
                        .tier(e.getGiftPool() != null ? e.getGiftPool().getTier() : null)
                        .build())
                .collect(Collectors.toList());

        return ClaimEggResponse.builder()
                .accounts(claimedAccounts)
                .stuckCount(stuckCount)
                .message(message)
                .build();
    }
}