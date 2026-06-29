package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.SyncKiotvietOrderUseCase;
import com.project.anhgagifcode.application.port.in.dto.EggDisplayDto;
import com.project.anhgagifcode.application.port.in.dto.SyncOrderResponse;
import com.project.anhgagifcode.application.port.out.*;
import com.project.anhgagifcode.domain.exception.BusinessRuleViolationException;
import com.project.anhgagifcode.domain.exception.ResourceNotFoundException;
import com.project.anhgagifcode.domain.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class SyncKiotvietOrderService implements SyncKiotvietOrderUseCase {

    private final KiotvietOrderPersistencePort orderPort;
    private final KiotvietApiPort apiPort;
    private final CustomerPersistencePort customerPort;
    private final ProductEggMappingPersistencePort mappingPort;
    private final EggPersistencePort eggPort;

    @Override
    @Transactional
    public SyncOrderResponse syncAndGetOrderDetails(String orderCode) {
        Optional<KiotvietOrder> existingOrderOpt = orderPort.loadByOrderCode(orderCode);
        KiotvietOrder currentOrder;
        Customer customer;

        if (existingOrderOpt.isEmpty()) {
            // First time sync, calling API
            KiotvietOrder apiOrder = apiPort.fetchOrderFromKiotviet(orderCode)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã đơn hàng này trên hệ thống KiotViet."));

            if (apiOrder.getCustomerCode() == null || apiOrder.getCustomerCode().trim().isEmpty() || "KHACH_LE".equalsIgnoreCase(apiOrder.getCustomerCode().trim())) {
                throw new BusinessRuleViolationException("Thiếu thông tin khách hàng");
            }

            // Process Customer Logic (success count, return streak, warning, banned)
            customer = processCustomerLogic(apiOrder.getCustomerCode(), apiOrder.getDeliveryStatus(), Optional.empty());
            
            apiOrder.setId(UUID.randomUUID().toString());
            apiOrder.setLastSyncedAt(LocalDateTime.now());
            apiOrder.setUpdatedAt(LocalDateTime.now());
            currentOrder = orderPort.saveOrder(apiOrder);

            // Block BANNED customers right at the entry gate
            if ("BANNED".equals(customer.getStatus())) {
                throw new BusinessRuleViolationException("Tài khoản bị khóa do vi phạm chính sách");
            }

            // Generate eggs
            generateEggsIfNeeded(currentOrder);
        } else {
            currentOrder = existingOrderOpt.get();
            if (currentOrder.getCustomerCode() == null || currentOrder.getCustomerCode().trim().isEmpty() || "KHACH_LE".equalsIgnoreCase(currentOrder.getCustomerCode().trim())) {
                throw new BusinessRuleViolationException("Thiếu thông tin khách hàng");
            }
            // Sync with API if cache expired (> 5 minutes)
            currentOrder = syncOrderIfNeeded(currentOrder);
            
            customer = customerPort.loadByCustomerCode(currentOrder.getCustomerCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Lỗi dữ liệu khách hàng."));

            // Block BANNED customers
            if ("BANNED".equals(customer.getStatus())) {
                throw new BusinessRuleViolationException("Tài khoản bị khóa do vi phạm chính sách");
            }

            // Generate eggs if not already generated
            generateEggsIfNeeded(currentOrder);
        }

        // Calculate display status
        List<Egg> eggs = eggPort.loadEggsByOrderId(currentOrder.getId());
        List<EggDisplayDto> eggDtos = calculateDisplayStatus(eggs, customer);

        return SyncOrderResponse.builder()
                .customerName(customer.getCustomerName() != null ? customer.getCustomerName() : "Khách hàng")
                .customerStatus(customer.getStatus())
                .deliveryStatus(currentOrder.getDeliveryStatus())
                .eggs(eggDtos)
                .build();
    }

    private Customer processCustomerLogic(String customerCode, String newStatus, Optional<KiotvietOrder> existingOrderOpt) {
        if (customerCode == null || customerCode.trim().isEmpty()) {
            customerCode = "KHACH_LE";
        }
        final String finalCustomerCode = customerCode.trim();
        Customer customer = customerPort.loadByCustomerCode(finalCustomerCode).orElseGet(() -> {
            Customer newCus = new Customer();
            newCus.setId(UUID.randomUUID().toString());
            newCus.setCustomerCode(finalCustomerCode);
            newCus.setStatus("NEW");
            newCus.setSuccessCount(0);
            newCus.setReturnStreak(0);
            newCus.setWarningCount(0);
            newCus.setCreatedAt(LocalDateTime.now());
            return newCus;
        });

        String oldStatus = existingOrderOpt.map(KiotvietOrder::getDeliveryStatus).orElse("");
        if (newStatus.equals(oldStatus)) {
            return customer;
        }

        boolean wasDeliveredBefore = "Đã giao hàng".equalsIgnoreCase(oldStatus) || "Giao thành công".equalsIgnoreCase(oldStatus);
        boolean isDeliveredNow = "Đã giao hàng".equalsIgnoreCase(newStatus) || "Giao thành công".equalsIgnoreCase(newStatus);

        boolean wasReturnedBefore = "Đang chuyển hoàn".equalsIgnoreCase(oldStatus) || "Đã chuyển hoàn".equalsIgnoreCase(oldStatus);
        boolean isReturnedNow = "Đang chuyển hoàn".equalsIgnoreCase(newStatus) || "Đã chuyển hoàn".equalsIgnoreCase(newStatus);

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
        } else if (isReturnedNow && !wasReturnedBefore) {
            customer.setReturnStreak(customer.getReturnStreak() + 1);
            if (customer.getReturnStreak() == 1) {
                customer.setStatus("WARNING");
            } else if (customer.getReturnStreak() >= 2) {
                customer.setStatus("BANNED");
            }
        }

        return customerPort.saveCustomer(customer);
    }

    private KiotvietOrder syncOrderIfNeeded(KiotvietOrder order) {
        if (order.getLastSyncedAt() != null && order.getLastSyncedAt().plusMinutes(5).isAfter(LocalDateTime.now())) {
            return order;
        }

        log.info("Đang đồng bộ lại đơn hàng {} do quá hạn 5 phút cache.", order.getOrderCode());

        // Fetch fresh order details from Kiotviet API
        KiotvietOrder apiOrder = apiPort.fetchOrderFromKiotviet(order.getOrderCode())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã đơn hàng này trên KiotViet."));

        if (apiOrder.getCustomerCode() == null || apiOrder.getCustomerCode().trim().isEmpty() || "KHACH_LE".equalsIgnoreCase(apiOrder.getCustomerCode().trim())) {
            throw new BusinessRuleViolationException("Thiếu thông tin khách hàng");
        }

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

    private void generateEggsIfNeeded(KiotvietOrder order) {
        List<Egg> existingEggs = eggPort.loadEggsByOrderId(order.getId());
        if (!existingEggs.isEmpty()) {
            return;
        }

        // Chỉ khi đơn hàng có trạng thái vận chuyển là "Giao thành công" (hoặc "Đã giao hàng") mới phát trứng
        String deliveryStatus = order.getDeliveryStatus();
        boolean isDelivered = "Đã giao hàng".equalsIgnoreCase(deliveryStatus) || "Giao thành công".equalsIgnoreCase(deliveryStatus);
        if (!isDelivered) {
            return;
        }

        List<String> productIds = order.getOrderItems().stream()
                .map(KiotvietOrderItem::getKvProductId).collect(Collectors.toList());

        List<ProductEggMapping> mappings = mappingPort.loadMappingsByProductIds(productIds);
        if (mappings.isEmpty()) {
            return;
        }

        // Group mappings by eggType
        List<ProductEggMapping> type1Mappings = mappings.stream()
                .filter(m -> m.getEggType() == 1)
                .collect(Collectors.toList());

        List<ProductEggMapping> type2Mappings = mappings.stream()
                .filter(m -> m.getEggType() == 2)
                .collect(Collectors.toList());

        Customer customer = customerPort.loadByCustomerCode(order.getCustomerCode())
                .orElseThrow(() -> new ResourceNotFoundException("Lỗi dữ liệu khách hàng."));

        // Generate Egg 1 if configured
        if (!type1Mappings.isEmpty()) {
            List<String> tiers = type1Mappings.stream()
                    .map(m -> m.getGiftPoolId().getTier())
                    .collect(Collectors.toList());
            String selectedTier = drawTier(tiers, 0.99); // 99% lowest tier, 1% higher
            ProductEggMapping selectedMapping = type1Mappings.stream()
                    .filter(m -> m.getGiftPoolId().getTier().equalsIgnoreCase(selectedTier))
                    .findFirst()
                    .orElse(type1Mappings.get(0));

            LocalDateTime hatchAt = null; // Clean customers have no cooldown
            if (customer.getReturnStreak() == 1) {
                hatchAt = LocalDateTime.now().plusDays(15); // Warning customers have 15-day cooldown
            }

            Egg newEgg = Egg.builder()
                    .id(UUID.randomUUID().toString())
                    .order(order)
                    .giftPool(selectedMapping.getGiftPoolId())
                    .eggType(1)
                    .status("PENDING")
                    .createdAt(LocalDateTime.now())
                    .hatchAt(hatchAt)
                    .build();
            eggPort.saveEgg(newEgg);
        }

        // Generate Egg 2 if configured
        if (!type2Mappings.isEmpty()) {
            List<String> tiers = type2Mappings.stream()
                    .map(m -> m.getGiftPoolId().getTier())
                    .collect(Collectors.toList());
            String selectedTier = drawTier(tiers, 0.95); // 95% lowest tier, 5% higher
            ProductEggMapping selectedMapping = type2Mappings.stream()
                    .filter(m -> m.getGiftPoolId().getTier().equalsIgnoreCase(selectedTier))
                    .findFirst()
                    .orElse(type2Mappings.get(0));

            LocalDateTime hatchAt = LocalDateTime.now().plusDays(15); // Always 15 days cooldown

            Egg newEgg = Egg.builder()
                    .id(UUID.randomUUID().toString())
                    .order(order)
                    .giftPool(selectedMapping.getGiftPoolId())
                    .eggType(2)
                    .status("PENDING")
                    .createdAt(LocalDateTime.now())
                    .hatchAt(hatchAt)
                    .build();
            eggPort.saveEgg(newEgg);
        }
    }

    private String drawTier(List<String> tiers, double lowestChance) {
        if (tiers == null || tiers.isEmpty()) {
            return null;
        }
        List<String> sortedTiers = tiers.stream().distinct().sorted().collect(Collectors.toList());
        if (sortedTiers.size() == 1) {
            return sortedTiers.get(0);
        }
        String lowest = sortedTiers.get(0);
        List<String> higher = sortedTiers.subList(1, sortedTiers.size());

        if (Math.random() < lowestChance) {
            return lowest;
        } else {
            int idx = (int) (Math.random() * higher.size());
            return higher.get(idx);
        }
    }

    private List<EggDisplayDto> calculateDisplayStatus(List<Egg> eggs, Customer customer) {
        List<EggDisplayDto> result = new ArrayList<>();

        for (Egg egg : eggs) {
            String displayStatus = egg.getStatus();
            LocalDateTime hatchAt = egg.getHatchAt();

            if ("CLAIMED".equals(displayStatus)) {
                // Keep CLAIMED
            } else if ("CANCELLED".equals(displayStatus)) {
                // Keep CANCELLED
            } else {
                // Cooldown Check
                boolean inHatchCooldown = hatchAt != null && LocalDateTime.now().isBefore(hatchAt);
                
                if (inHatchCooldown) {
                    displayStatus = "HATCHING";
                } else {
                    // Ready to claim conditions
                    boolean isClean = customer.getReturnStreak() == 0;
                    if (egg.getEggType() == 1 && isClean) {
                        displayStatus = "READY_TO_CLAIM";
                    } else {
                        // Check if absolute success
                        if (isAbsoluteSuccess(egg.getOrder())) {
                            displayStatus = "READY_TO_CLAIM";
                        } else {
                            displayStatus = "WAITING_ORDER_COMPLETION";
                        }
                    }
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

    private boolean isAbsoluteSuccess(KiotvietOrder order) {
        if (!"Đã giao hàng".equalsIgnoreCase(order.getDeliveryStatus())) {
            return false;
        }
        LocalDateTime deliveryDate = order.getUpdatedAt() != null ? order.getUpdatedAt() : order.getCreatedAt();
        return deliveryDate.plusDays(15).isBefore(LocalDateTime.now());
    }
}
