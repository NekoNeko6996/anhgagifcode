package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.ClaimEggUseCase;
import com.project.anhgagifcode.application.port.in.dto.ClaimEggResponse;
import com.project.anhgagifcode.application.port.out.*;
import com.project.anhgagifcode.domain.exception.BusinessRuleViolationException;
import com.project.anhgagifcode.domain.exception.ResourceNotFoundException;
import com.project.anhgagifcode.domain.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class ClaimEggService implements ClaimEggUseCase {

    private final EggPersistencePort eggPort;
    private final GiftAccountPersistencePort accountPort;
    private final EggOpeningLogPersistencePort logPort;
    private final KiotvietOrderPersistencePort orderPort;
    private final KiotvietApiPort apiPort;
    private final CustomerPersistencePort customerPort;
    
    private final Random random = new Random();

    @Override
    @Transactional
    public ClaimEggResponse claimEggReward(String eggId, String ipAddress) {
        
        // 1. Load và Khóa Trứng (Lock For Update)
        Egg egg = eggPort.loadEggForUpdate(eggId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trứng hợp lệ."));

        // 2. Đồng bộ trạng thái đơn hàng thời gian thực nếu cache quá hạn 5 phút
        KiotvietOrder order = syncOrderIfNeeded(egg.getOrder());

        // 3. Load thông tin khách hàng mới nhất
        Customer customer = customerPort.loadByCustomerCode(order.getCustomerCode())
                .orElseThrow(() -> new ResourceNotFoundException("Lỗi dữ liệu khách hàng."));

        // 4. Kiểm tra trạng thái cấm (BANNED)
        if ("BANNED".equals(customer.getStatus())) {
            throw new BusinessRuleViolationException("Tài khoản bị khóa do vi phạm chính sách");
        }

        // 5. Kiểm tra trạng thái trứng
        if ("CLAIMED".equals(egg.getStatus()) || "CANCELLED".equals(egg.getStatus())) {
            throw new BusinessRuleViolationException("Trứng này đã được mở hoặc bị hủy.");
        }

        // 6. Kiểm tra điều kiện thời gian ấp (Hatching Cooldown)
        boolean inHatchCooldown = egg.getHatchAt() != null && LocalDateTime.now().isBefore(egg.getHatchAt());
        if (inHatchCooldown) {
            throw new BusinessRuleViolationException("Trứng đang ấp, chưa đến thời gian mở.");
        }

        // 7. Kiểm tra trạng thái đơn hàng đối soát (Absolute Success)
        boolean isClean = customer.getReturnStreak() == 0;
        if (egg.getEggType() == 1 && isClean) {
            // Đối với trứng số 1 của khách hàng AN TOÀN: Chỉ cần đơn hàng không hoàn trả
            boolean isReturned = "Đang chuyển hoàn".equalsIgnoreCase(order.getDeliveryStatus()) 
                    || "Đã chuyển hoàn".equalsIgnoreCase(order.getDeliveryStatus());
            if (isReturned) {
                throw new BusinessRuleViolationException("Đơn hàng này đã bị hoàn/trả.");
            }
        } else {
            // Trứng số 2 hoặc trứng số 1 của khách hàng CẢNH CÁO: Phải đạt trạng thái thành công tuyệt đối
            if (!isAbsoluteSuccess(order)) {
                throw new BusinessRuleViolationException("Đơn hàng chưa đạt trạng thái thành công tuyệt đối hoặc chưa đủ 15 ngày.");
            }
        }

        // 8. Đếm số lượng quà trong Kho
        String poolId = egg.getGiftPool().getId();
        long availableCount = accountPort.countAvailableAccountsByPoolId(poolId);
        
        if (availableCount == 0) {
            throw new BusinessRuleViolationException("Kho quà hiện tại đã hết, vui lòng quay lại sau.");
        }

        // 9. Bốc Quà (Lock For Update GiftAccount)
        int randomOffset = random.nextInt((int) availableCount);
        GiftAccount assignedAccount = accountPort.pickRandomAvailableAccountForUpdate(poolId, randomOffset)
                .orElseThrow(() -> new BusinessRuleViolationException("Lỗi hệ thống khi bốc quà, vui lòng thử lại."));

        // 10. Cập nhật trạng thái tài khoản quà tặng
        assignedAccount.setStatus("ASSIGNED");
        assignedAccount.setAssignedAt(LocalDateTime.now());
        accountPort.updateAccount(assignedAccount);

        // 11. Cập nhật thông tin trứng
        egg.setStatus("CLAIMED");
        egg.setAccount(assignedAccount);
        eggPort.saveEgg(egg);

        // 12. Ghi Log hệ thống
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

        // 13. Xử lý điều kiện Ân xá (Reset Streak) cho khách hàng WARNING
        if (customer.getReturnStreak() == 1) {
            processAmnesty(customer);
        }

        return ClaimEggResponse.builder()
                .username(assignedAccount.getUsername())
                .password(assignedAccount.getPassword())
                .platform(assignedAccount.getPlatform())
                .message("Chúc mừng! Bạn đã mở trứng thành công.")
                .build();
    }

    private KiotvietOrder syncOrderIfNeeded(KiotvietOrder order) {
        if (order.getLastSyncedAt() != null && order.getLastSyncedAt().plusMinutes(5).isAfter(LocalDateTime.now())) {
            return order;
        }

        log.info("Đang đồng bộ lại đơn hàng {} do quá hạn 5 phút cache.", order.getOrderCode());

        // Fetch fresh order details from Kiotviet API
        KiotvietOrder apiOrder = apiPort.fetchOrderFromKiotviet(order.getOrderCode())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã đơn hàng này trên KiotViet."));

        // Determine transitions
        boolean wasReturnedBefore = "Đang chuyển hoàn".equalsIgnoreCase(order.getDeliveryStatus()) 
                || "Đã chuyển hoàn".equalsIgnoreCase(order.getDeliveryStatus());
        boolean isReturnedNow = "Đang chuyển hoàn".equalsIgnoreCase(apiOrder.getDeliveryStatus()) 
                || "Đã chuyển hoàn".equalsIgnoreCase(apiOrder.getDeliveryStatus());

        boolean wasDeliveredBefore = "Đã giao hàng".equalsIgnoreCase(order.getDeliveryStatus())
                || "Giao thành công".equalsIgnoreCase(order.getDeliveryStatus());
        boolean isDeliveredNow = "Đã giao hàng".equalsIgnoreCase(apiOrder.getDeliveryStatus())
                || "Giao thành công".equalsIgnoreCase(apiOrder.getDeliveryStatus());

        // Update delivery status and updatedAt
        order.setDeliveryStatus(apiOrder.getDeliveryStatus());
        if ("Đã giao hàng".equalsIgnoreCase(apiOrder.getDeliveryStatus())) {
            if (order.getUpdatedAt() == null || !wasDeliveredBefore) {
                order.setUpdatedAt(LocalDateTime.now());
            }
        } else {
            order.setUpdatedAt(LocalDateTime.now());
        }
        order.setLastSyncedAt(LocalDateTime.now());

        // Update items if any
        if (apiOrder.getOrderItems() != null) {
            order.setOrderItems(apiOrder.getOrderItems());
        }

        KiotvietOrder updatedOrder = orderPort.saveOrder(order);

        // Process Customer Logic (success count, return streak, warning, banned)
        Customer customer = customerPort.loadByCustomerCode(order.getCustomerCode()).orElseGet(() -> {
            Customer newCus = new Customer();
            newCus.setId(UUID.randomUUID().toString());
            newCus.setCustomerCode(order.getCustomerCode());
            newCus.setStatus("NEW");
            newCus.setSuccessCount(0);
            newCus.setReturnStreak(0);
            newCus.setWarningCount(0);
            newCus.setCreatedAt(LocalDateTime.now());
            return newCus;
        });

        if (isDeliveredNow && !wasDeliveredBefore) {
            customer.setSuccessCount(customer.getSuccessCount() + 1);
            if (customer.getReturnStreak() == 0) {
                if (customer.getSuccessCount() >= 5) {
                    customer.setStatus("TRUSTED_2");
                } else if (customer.getSuccessCount() >= 2) {
                    customer.setStatus("TRUSTED_1");
                } else {
                    customer.setStatus("NEW");
                }
            }
        }

        if (isReturnedNow && !wasReturnedBefore) {
            customer.setReturnStreak(customer.getReturnStreak() + 1);
            if (customer.getReturnStreak() == 1) {
                customer.setStatus("WARNING");
            } else if (customer.getReturnStreak() >= 2) {
                customer.setStatus("BANNED");
            }

            // Cancel all eggs for this order
            List<Egg> eggs = eggPort.loadEggsByOrderId(order.getId());
            for (Egg egg : eggs) {
                egg.setStatus("CANCELLED");
                eggPort.saveEgg(egg);
            }
        }

        customerPort.saveCustomer(customer);
        return updatedOrder;
    }

    private boolean isAbsoluteSuccess(KiotvietOrder order) {
        if (!"Đã giao hàng".equalsIgnoreCase(order.getDeliveryStatus())) {
            return false;
        }
        LocalDateTime deliveryDate = order.getUpdatedAt() != null ? order.getUpdatedAt() : order.getCreatedAt();
        return deliveryDate.plusDays(15).isBefore(LocalDateTime.now());
    }

    private void processAmnesty(Customer customer) {
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