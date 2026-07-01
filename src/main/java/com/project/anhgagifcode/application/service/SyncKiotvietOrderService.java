package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.SyncKiotvietOrderUseCase;
import com.project.anhgagifcode.application.port.in.dto.EggDisplayDto;
import com.project.anhgagifcode.application.port.in.dto.SyncOrderResponse;
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
public class SyncKiotvietOrderService implements SyncKiotvietOrderUseCase {

    private final KiotvietOrderPersistencePort orderPort;
    private final KiotvietApiPort apiPort;
    private final CustomerPersistencePort customerPort;
    private final ProductEggMappingPersistencePort mappingPort;
    private final EggPersistencePort eggPort;
    private final TransactionTemplate transactionTemplate;

    public SyncKiotvietOrderService(
            KiotvietOrderPersistencePort orderPort,
            KiotvietApiPort apiPort,
            CustomerPersistencePort customerPort,
            ProductEggMappingPersistencePort mappingPort,
            EggPersistencePort eggPort,
            PlatformTransactionManager transactionManager) {
        this.orderPort = orderPort;
        this.apiPort = apiPort;
        this.customerPort = customerPort;
        this.mappingPort = mappingPort;
        this.eggPort = eggPort;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    private static class SyncDataHolder {
        final KiotvietOrder order;
        final Customer customer;
        SyncDataHolder(KiotvietOrder order, Customer customer) {
            this.order = order;
            this.customer = customer;
        }
    }

    @Override
    public SyncOrderResponse syncAndGetOrderDetails(String orderCode) {
        Optional<KiotvietOrder> existingOrderOpt = orderPort.loadByOrderCode(orderCode);
        KiotvietOrder currentOrder;
        Customer customer;

        if (existingOrderOpt.isEmpty()) {
            // 1. Gọi API ngoài transaction để lấy thông tin đơn hàng mới
            java.util.Set<String> candidateCodes = new java.util.LinkedHashSet<>();
            candidateCodes.add(orderCode); // Thử tìm chính xác trước

            // Nếu mã không giống mã đầy đủ (không chứa '_' và không bắt đầu bằng các tiền tố quen thuộc), thử thêm tiền tố
            boolean looksLikeFullCode = orderCode.contains("_")
                    || orderCode.startsWith("HD")
                    || orderCode.startsWith("DH")
                    || orderCode.startsWith("OD");

            if (!looksLikeFullCode) {
                java.util.List<String> dbPrefixes = orderPort.findDistinctPrefixes();
                if (dbPrefixes == null) {
                    dbPrefixes = java.util.Collections.emptyList();
                }
                java.util.List<String> defaultPrefixes = java.util.List.of("HDTTS", "HDSPE", "DHTTS", "DHSPE", "HD", "DH", "OD");
                java.util.List<String> allPrefixes = new java.util.ArrayList<>(dbPrefixes);
                for (String def : defaultPrefixes) {
                    if (!allPrefixes.contains(def)) {
                        allPrefixes.add(def);
                    }
                }
                for (String prefix : allPrefixes) {
                    candidateCodes.add(prefix + "_" + orderCode);
                    candidateCodes.add(prefix + orderCode);
                }
            }

            Optional<KiotvietOrder> apiOrderOpt = Optional.empty();
            for (String candidate : candidateCodes) {
                apiOrderOpt = apiPort.fetchOrderFromKiotviet(candidate);
                if (apiOrderOpt.isPresent()) {
                    break;
                }
            }

            KiotvietOrder apiOrder = apiOrderOpt
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã đơn hàng này trên hệ thống KiotViet."));

            if (apiOrder.getCustomerCode() == null || apiOrder.getCustomerCode().trim().isEmpty() || "KHACH_LE".equalsIgnoreCase(apiOrder.getCustomerCode().trim())) {
                throw new BusinessRuleViolationException("Thiếu thông tin khách hàng");
            }

            final KiotvietOrder finalApiOrder = apiOrder;
            // 2. Chạy ghi database trong transaction
            SyncDataHolder holder = transactionTemplate.execute(status -> {
                Customer cus = processCustomerLogic(finalApiOrder.getCustomerCode(), finalApiOrder.getDeliveryStatus(), Optional.empty());
                finalApiOrder.setId(UUID.randomUUID().toString());
                finalApiOrder.setLastSyncedAt(LocalDateTime.now());
                finalApiOrder.setUpdatedAt(LocalDateTime.now());
                KiotvietOrder ord = orderPort.saveOrder(finalApiOrder);

                // Khóa tài khoản bị BANNED
                if ("BANNED".equals(cus.getStatus())) {
                    throw new BusinessRuleViolationException("Tài khoản bị khóa do vi phạm chính sách");
                }

                generateEggsIfNeeded(ord);
                return new SyncDataHolder(ord, cus);
            });
            currentOrder = holder.order;
            customer = holder.customer;
        } else {
            currentOrder = existingOrderOpt.get();
            if (currentOrder.getCustomerCode() == null || currentOrder.getCustomerCode().trim().isEmpty() || "KHACH_LE".equalsIgnoreCase(currentOrder.getCustomerCode().trim())) {
                throw new BusinessRuleViolationException("Thiếu thông tin khách hàng");
            }
            // 1. Đồng bộ lại qua API ngoài transaction nếu cache hết hạn
            currentOrder = syncOrderIfNeeded(currentOrder);
            
            // 2. Load trạng thái khách hàng hiện tại
            customer = customerPort.loadByCustomerCode(currentOrder.getCustomerCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Lỗi dữ liệu khách hàng."));

            // Khóa tài khoản bị BANNED
            if ("BANNED".equals(customer.getStatus())) {
                throw new BusinessRuleViolationException("Tài khoản bị khóa do vi phạm chính sách");
            }

            // 3. Phát sinh trứng trong transaction
            final KiotvietOrder finalOrder = currentOrder;
            transactionTemplate.executeWithoutResult(status -> {
                generateEggsIfNeeded(finalOrder);
            });
        }

        // Tải trứng hiển thị
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

        // LOGIC LOYALTY SỬA ĐỔI: Tăng khi giao thành công, giảm nếu bị hoàn trả hoặc trạng thái chuyển về chưa giao
        if (isDeliveredNow && !wasDeliveredBefore) {
            customer.setSuccessCount(customer.getSuccessCount() + 1);
        } else if (!isDeliveredNow && wasDeliveredBefore) {
            customer.setSuccessCount(Math.max(0, customer.getSuccessCount() - 1));
        }

        if (customer.getReturnStreak() == 0) {
            if (customer.getSuccessCount() >= 5) {
                customer.setStatus("TRUSTED_2");
            } else if (customer.getSuccessCount() >= 2) {
                customer.setStatus("TRUSTED_1");
            } else {
                customer.setStatus("NEW");
            }
        }

        if (isReturnedNow && !wasReturnedBefore) {
            customer.setReturnStreak(customer.getReturnStreak() + 1);
            if (customer.getReturnStreak() == 1) {
                customer.setStatus("WARNING");
            } else if (customer.getReturnStreak() >= 2) {
                customer.setStatus("BANNED");
            }
        }

        return customerPort.saveCustomer(customer);
    }

    @Override
    public KiotvietOrder syncOrderIfNeeded(KiotvietOrder order) {
        if (order.getLastSyncedAt() != null && order.getLastSyncedAt().plusMinutes(5).isAfter(LocalDateTime.now())) {
            return order;
        }

        log.info("Đang đồng bộ lại đơn hàng {} do quá hạn 5 phút cache.", order.getOrderCode());

        // 1. Gọi API KiotViet ngoài Transaction
        KiotvietOrder apiOrder = apiPort.fetchOrderFromKiotviet(order.getOrderCode())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã đơn hàng này trên KiotViet."));

        if (apiOrder.getCustomerCode() == null || apiOrder.getCustomerCode().trim().isEmpty() || "KHACH_LE".equalsIgnoreCase(apiOrder.getCustomerCode().trim())) {
            throw new BusinessRuleViolationException("Thiếu thông tin khách hàng");
        }

        // 2. Chạy cập nhật database trong Transaction
        return transactionTemplate.execute(status -> {
            KiotvietOrder dbOrder = orderPort.loadByOrderCode(order.getOrderCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại trong database."));

            // Tính toán chuyển đổi trạng thái
            boolean wasReturnedBefore = "Đang chuyển hoàn".equalsIgnoreCase(dbOrder.getDeliveryStatus()) 
                    || "Đã chuyển hoàn".equalsIgnoreCase(dbOrder.getDeliveryStatus());
            boolean isReturnedNow = "Đang chuyển hoàn".equalsIgnoreCase(apiOrder.getDeliveryStatus()) 
                    || "Đã chuyển hoàn".equalsIgnoreCase(apiOrder.getDeliveryStatus());

            boolean wasDeliveredBefore = "Đã giao hàng".equalsIgnoreCase(dbOrder.getDeliveryStatus())
                    || "Giao thành công".equalsIgnoreCase(dbOrder.getDeliveryStatus());
            boolean isDeliveredNow = "Đã giao hàng".equalsIgnoreCase(apiOrder.getDeliveryStatus())
                    || "Giao thành công".equalsIgnoreCase(apiOrder.getDeliveryStatus());

            dbOrder.setDeliveryStatus(apiOrder.getDeliveryStatus());
            if ("Đã giao hàng".equalsIgnoreCase(apiOrder.getDeliveryStatus())) {
                if (dbOrder.getUpdatedAt() == null || !wasDeliveredBefore) {
                    dbOrder.setUpdatedAt(LocalDateTime.now());
                }
            } else {
                dbOrder.setUpdatedAt(LocalDateTime.now());
            }
            dbOrder.setLastSyncedAt(LocalDateTime.now());

            if (apiOrder.getOrderItems() != null) {
                dbOrder.setOrderItems(apiOrder.getOrderItems());
            }

            KiotvietOrder updatedOrder = orderPort.saveOrder(dbOrder);

            Customer customer = customerPort.loadByCustomerCode(dbOrder.getCustomerCode()).orElseGet(() -> {
                Customer newCus = new Customer();
                newCus.setId(UUID.randomUUID().toString());
                newCus.setCustomerCode(dbOrder.getCustomerCode());
                newCus.setStatus("NEW");
                newCus.setSuccessCount(0);
                newCus.setReturnStreak(0);
                newCus.setWarningCount(0);
                newCus.setCreatedAt(LocalDateTime.now());
                return newCus;
            });

            // LOGIC LOYALTY SỬA ĐỔI
            if (isDeliveredNow && !wasDeliveredBefore) {
                customer.setSuccessCount(customer.getSuccessCount() + 1);
            } else if (!isDeliveredNow && wasDeliveredBefore) {
                customer.setSuccessCount(Math.max(0, customer.getSuccessCount() - 1));
            }

            if (customer.getReturnStreak() == 0) {
                if (customer.getSuccessCount() >= 5) {
                    customer.setStatus("TRUSTED_2");
                } else if (customer.getSuccessCount() >= 2) {
                    customer.setStatus("TRUSTED_1");
                } else {
                    customer.setStatus("NEW");
                }
            }

            if (isReturnedNow && !wasReturnedBefore) {
                customer.setReturnStreak(customer.getReturnStreak() + 1);
                if (customer.getReturnStreak() == 1) {
                    customer.setStatus("WARNING");
                } else if (customer.getReturnStreak() >= 2) {
                    customer.setStatus("BANNED");
                }

                // Hủy tất cả trứng khi đơn hàng bị hoàn/trả
                List<Egg> eggs = eggPort.loadEggsByOrderId(dbOrder.getId());
                for (Egg egg : eggs) {
                    egg.setStatus("CANCELLED");
                    eggPort.saveEgg(egg);
                }
            }

            customerPort.saveCustomer(customer);
            return updatedOrder;
        });
    }

    private void generateEggsIfNeeded(KiotvietOrder order) {
        List<Egg> existingEggs = eggPort.loadEggsByOrderId(order.getId());
        if (!existingEggs.isEmpty()) {
            return;
        }

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

        Customer customer = customerPort.loadByCustomerCode(order.getCustomerCode())
                .orElseThrow(() -> new ResourceNotFoundException("Lỗi dữ liệu khách hàng."));

        // Draw pool for Egg 1 (Type 1)
        ProductEggMapping mapping1 = drawMappingFromList(mappings);
        if (mapping1 != null) {
            LocalDateTime hatchAt = null;
            if (customer.getReturnStreak() == 1) {
                hatchAt = LocalDateTime.now().plusDays(15);
            }

            String initialStatus = "READY_TO_CLAIM";
            if (hatchAt != null && LocalDateTime.now().isBefore(hatchAt)) {
                initialStatus = "HATCHING";
            } else {
                boolean isClean = customer.getReturnStreak() == 0;
                if (!isClean) {
                    initialStatus = "WAITING_ORDER_COMPLETION";
                }
            }

            Egg egg1 = Egg.builder()
                    .id(UUID.randomUUID().toString())
                    .order(order)
                    .giftPool(mapping1.getGiftPoolId())
                    .eggType(1)
                    .status(initialStatus)
                    .createdAt(LocalDateTime.now())
                    .hatchAt(hatchAt)
                    .build();
            eggPort.saveEgg(egg1);
        }

        // Draw pool for Egg 2 (Type 2)
        ProductEggMapping mapping2 = drawMappingFromList(mappings);
        if (mapping2 != null) {
            LocalDateTime hatchAt = LocalDateTime.now().plusDays(15);

            Egg egg2 = Egg.builder()
                    .id(UUID.randomUUID().toString())
                    .order(order)
                    .giftPool(mapping2.getGiftPoolId())
                    .eggType(2)
                    .status("HATCHING")
                    .createdAt(LocalDateTime.now())
                    .hatchAt(hatchAt)
                    .build();
            eggPort.saveEgg(egg2);
        }
    }

    private ProductEggMapping drawMappingFromList(List<ProductEggMapping> mappings) {
        if (mappings == null || mappings.isEmpty()) {
            return null;
        }
        double totalSum = mappings.stream().mapToDouble(ProductEggMapping::getRate).sum();
        if (totalSum <= 0.0) {
            return mappings.get(0);
        }
        double r = Math.random() * totalSum;
        double cumulative = 0.0;
        for (ProductEggMapping mapping : mappings) {
            cumulative += mapping.getRate();
            if (r <= cumulative) {
                return mapping;
            }
        }
        return mappings.get(mappings.size() - 1);
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
                boolean inHatchCooldown = hatchAt != null && LocalDateTime.now().isBefore(hatchAt);
                
                if (inHatchCooldown) {
                    displayStatus = "HATCHING";
                } else {
                    boolean isClean = customer.getReturnStreak() == 0;
                    if (egg.getEggType() == 1 && isClean) {
                        displayStatus = "READY_TO_CLAIM";
                    } else {
                        if (isAbsoluteSuccess(egg.getOrder())) {
                            displayStatus = "READY_TO_CLAIM";
                        } else {
                            displayStatus = "WAITING_ORDER_COMPLETION";
                        }
                    }
                }

                // Lưu lại trạng thái thực tế mới nhất vào database
                if (!displayStatus.equals(egg.getStatus())) {
                    egg.setStatus(displayStatus);
                    eggPort.saveEgg(egg);
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
