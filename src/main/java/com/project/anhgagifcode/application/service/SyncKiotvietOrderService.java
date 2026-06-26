package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.SyncKiotvietOrderUseCase;
import com.project.anhgagifcode.application.port.in.dto.EggDisplayDto;
import com.project.anhgagifcode.application.port.in.dto.SyncOrderResponse;
import com.project.anhgagifcode.application.port.out.*;
import com.project.anhgagifcode.domain.exception.BusinessRuleViolationException;
import com.project.anhgagifcode.domain.exception.ResourceNotFoundException;
import com.project.anhgagifcode.domain.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class SyncKiotvietOrderService implements SyncKiotvietOrderUseCase {

    private final KiotvietOrderPersistencePort orderPort;
    private final KiotvietApiPort apiPort;
    private final CustomerPersistencePort customerPort;
    private final ProductEggMappingPersistencePort mappingPort;
    private final EggPersistencePort eggPort;

    private static final int SYNC_COOLDOWN_MINUTES = 15;
    private static final int BASE_DELAY_HOURS = 24; // Giả sử trứng cấp 2 cần ấp 24h mặc định

    @Override
    @Transactional
    public SyncOrderResponse syncAndGetOrderDetails(String orderCode) {
        Optional<KiotvietOrder> existingOrderOpt = orderPort.loadByOrderCode(orderCode);
        KiotvietOrder currentOrder;
        Customer customer;

        boolean needsApiSync = existingOrderOpt.isEmpty()
                || existingOrderOpt.get().getLastSyncedAt().plusMinutes(SYNC_COOLDOWN_MINUTES).isBefore(LocalDateTime.now());

        if (needsApiSync) {
            // 1. Gọi API KiotViet
            KiotvietOrder apiOrder = apiPort.fetchOrderFromKiotviet(orderCode)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã đơn hàng này trên hệ thống."));

            // 2. Xử lý Customer Logic (Ban/Uy tín)
            customer = processCustomerLogic(apiOrder.getCustomerCode(), apiOrder.getDeliveryStatus(), existingOrderOpt);

            // 3. Cập nhật Đơn hàng
            if (existingOrderOpt.isPresent()) {
                apiOrder.setId(existingOrderOpt.get().getId());
            } else {
                apiOrder.setId(UUID.randomUUID().toString());
            }
            apiOrder.setLastSyncedAt(LocalDateTime.now());
            currentOrder = orderPort.saveOrder(apiOrder);

            // 4. Sinh trứng nếu chưa có
            generateEggsIfNeeded(currentOrder);
        } else {
            currentOrder = existingOrderOpt.get();
            customer = customerPort.loadByCustomerCode(currentOrder.getCustomerCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Lỗi dữ liệu khách hàng."));
        }

        // Chặn nếu bị Ban
        if ("BANNED".equals(customer.getStatus())) {
            throw new BusinessRuleViolationException("Tài khoản của bạn đã bị cấm do tỷ lệ hoàn hàng quá cao.");
        }

        // 5. Tính toán trạng thái hiển thị
        List<Egg> eggs = eggPort.loadEggsByOrderId(currentOrder.getId());
        List<EggDisplayDto> eggDtos = calculateDisplayStatus(eggs, currentOrder.getDeliveryStatus(), customer);

        return SyncOrderResponse.builder()
                .customerName(customer.getCustomerName())
                .customerStatus(customer.getStatus())
                .deliveryStatus(currentOrder.getDeliveryStatus())
                .eggs(eggDtos)
                .build();
    }

    private Customer processCustomerLogic(String customerCode, String newStatus, Optional<KiotvietOrder> existingOrderOpt) {
        Customer customer = customerPort.loadByCustomerCode(customerCode).orElseGet(() -> {
            Customer newCus = new Customer();
            newCus.setId(UUID.randomUUID().toString());
            newCus.setCustomerCode(customerCode);
            newCus.setStatus("NEW");
            newCus.setSuccessCount(0);
            newCus.setReturnStreak(0);
            newCus.setWarningCount(0);
            newCus.setCreatedAt(LocalDateTime.now());
            return newCus;
        });

        // Chỉ cộng điểm nếu trạng thái đơn hàng thay đổi
        String oldStatus = existingOrderOpt.map(KiotvietOrder::getDeliveryStatus).orElse("");
        if (newStatus.equals(oldStatus)) {
            return customer;
        }

        if ("Giao thành công".equalsIgnoreCase(newStatus)) {
            customer.setSuccessCount(customer.getSuccessCount() + 1);
            customer.setReturnStreak(0); // Reset chuỗi hoàn

            if (customer.getSuccessCount() >= 5) {
                customer.setStatus("TRUSTED_2");
            } else if (customer.getSuccessCount() >= 2) {
                customer.setStatus("TRUSTED_1");
            }

        } else if ("Đang chuyển hoàn".equalsIgnoreCase(newStatus) || "Đã chuyển hoàn".equalsIgnoreCase(newStatus)) {
            customer.setReturnStreak(customer.getReturnStreak() + 1);

            if (customer.getSuccessCount() == 0 && customer.getReturnStreak() >= 2) {
                customer.setStatus("BANNED");
            } else if (customer.getSuccessCount() > 0 && customer.getReturnStreak() == 1) {
                customer.setStatus("NEW"); // Mất uy tín
            } else if (customer.getSuccessCount() > 0 && customer.getReturnStreak() == 2) {
                customer.setStatus("WARNING");
                customer.setWarningCount(customer.getWarningCount() + 1);
                customer.setReturnStreak(0); // Reset để đếm lại
                if (customer.getWarningCount() >= 2) {
                    customer.setStatus("BANNED");
                }
            }
        }
        return customerPort.saveCustomer(customer);
    }

    private void generateEggsIfNeeded(KiotvietOrder order) {
        List<String> productIds = order.getOrderItems().stream()
                .map(KiotvietOrderItem::getKvProductId).collect(Collectors.toList());

        List<ProductEggMapping> mappings = mappingPort.loadMappingsByProductIds(productIds);

        for (ProductEggMapping mapping : mappings) {
            // CẬP NHẬT Ở ĐÂY: Thêm mapping.getEggType() vào logic check
            if (!eggPort.existsByOrderIdAndPoolIdAndEggType(order.getId(), mapping.getGiftPool().getId(), mapping.getEggType())) {

                Egg newEgg = Egg.builder()
                        .id(UUID.randomUUID().toString())
                        .order(order)
                        .giftPool(mapping.getGiftPool())
                        .eggType(mapping.getEggType()) // Sẽ đẻ ra 1 trứng loại 1 và 1 trứng loại 2 bình thường
                        .status("PENDING")
                        .createdAt(LocalDateTime.now())
                        .build();
                eggPort.saveEgg(newEgg);
            }
        }
    }

    private List<EggDisplayDto> calculateDisplayStatus(List<Egg> eggs, String deliveryStatus, Customer customer) {
        List<EggDisplayDto> result = new ArrayList<>();
        boolean isReturned = "Đang chuyển hoàn".equalsIgnoreCase(deliveryStatus) || "Đã chuyển hoàn".equalsIgnoreCase(deliveryStatus);

        for (Egg egg : eggs) {
            String displayStatus = egg.getStatus();
            LocalDateTime hatchAt = egg.getHatchAt();

            if ("CLAIMED".equals(displayStatus)) {
                // Giữ nguyên nếu đã mở
            } else if (isReturned) {
                displayStatus = "CANCELLED";
                egg.setStatus("CANCELLED");
                eggPort.saveEgg(egg);
            } else if ("Đang giao hàng".equalsIgnoreCase(deliveryStatus)) {
                displayStatus = (egg.getEggType() == 1) ? "READY_TO_CLAIM" : "WAITING_ORDER_COMPLETION";
            } else if ("Giao thành công".equalsIgnoreCase(deliveryStatus)) {
                if (egg.getEggType() == 1) {
                    displayStatus = "READY_TO_CLAIM";
                } else {
                    if (hatchAt == null) {
                        // Tính toán Cooldown
                        int delayHours = BASE_DELAY_HOURS;
                        if ("TRUSTED_1".equals(customer.getStatus())) {
                            delayHours = BASE_DELAY_HOURS * 2 / 3;
                        } else if ("TRUSTED_2".equals(customer.getStatus())) {
                            delayHours = BASE_DELAY_HOURS / 2;
                        }

                        hatchAt = LocalDateTime.now().plusHours(delayHours);
                        egg.setHatchAt(hatchAt);
                        eggPort.saveEgg(egg);
                    }
                    displayStatus = LocalDateTime.now().isAfter(hatchAt) ? "READY_TO_CLAIM" : "HATCHING";
                }
            }

            result.add(EggDisplayDto.builder()
                    .eggId(egg.getId())
                    .eggType(egg.getEggType())
                    .displayStatus(displayStatus)
                    .hatchAt(hatchAt)
                    .build());
        }
        return result;
    }
}
