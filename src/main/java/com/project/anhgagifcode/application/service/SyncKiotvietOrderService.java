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
    private final NotificationPort notificationPort;
    private final KiotvietProductPersistencePort productPort;
    private final SystemConfigPersistencePort configPort;
    private final TransactionTemplate transactionTemplate;

    public SyncKiotvietOrderService(
            KiotvietOrderPersistencePort orderPort,
            KiotvietApiPort apiPort,
            CustomerPersistencePort customerPort,
            ProductEggMappingPersistencePort mappingPort,
            EggPersistencePort eggPort,
            NotificationPort notificationPort,
            KiotvietProductPersistencePort productPort,
            SystemConfigPersistencePort configPort,
            PlatformTransactionManager transactionManager) {
        this.orderPort = orderPort;
        this.apiPort = apiPort;
        this.customerPort = customerPort;
        this.mappingPort = mappingPort;
        this.eggPort = eggPort;
        this.notificationPort = notificationPort;
        this.productPort = productPort;
        this.configPort = configPort;
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
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã đơn hàng này trên hệ thống, vui lòng thử lại sau."));

            if (apiOrder.getCustomerCode() == null || apiOrder.getCustomerCode().trim().isEmpty() || "KHACH_LE".equalsIgnoreCase(apiOrder.getCustomerCode().trim())) {
                throw new BusinessRuleViolationException("Thiếu thông tin khách hàng");
            }

            // Load/register customer
            Customer cus = customerPort.loadByCustomerCode(apiOrder.getCustomerCode().trim()).orElseGet(() -> {
                Customer newCus = new Customer();
                newCus.setId(UUID.randomUUID().toString());
                newCus.setCustomerCode(apiOrder.getCustomerCode().trim());
                newCus.setStatus("NORMAL");
                newCus.setSuccessCount(0);
                newCus.setReturnStreak(0);
                newCus.setWarningCount(0);
                newCus.setEarlyHatchCredits(0);
                newCus.setReturnCount(0);
                newCus.setCreatedAt(LocalDateTime.now());
                return customerPort.saveCustomer(newCus);
            });

            // Khóa tài khoản bị BANNED hoặc TEMP_BANNED
            if ("BANNED".equalsIgnoreCase(cus.getStatus())) {
                throw new BusinessRuleViolationException("Tài khoản bị khóa do vi phạm chính sách");
            }
            if ("TEMP_BANNED".equalsIgnoreCase(cus.getStatus()) && cus.getUnbanAt() != null && LocalDateTime.now().isBefore(cus.getUnbanAt())) {
                throw new BusinessRuleViolationException("Tài khoản của bạn đang bị khóa tạm thời đến ngày " + cus.getUnbanAt() + ".");
            }

            final KiotvietOrder finalApiOrder = apiOrder;
            final Customer finalCustomer = cus;

            // 2. Chạy ghi database trong transaction
            SyncDataHolder holder = transactionTemplate.execute(status -> {
                finalApiOrder.setId(UUID.randomUUID().toString());
                finalApiOrder.setLastSyncedAt(LocalDateTime.now());
                finalApiOrder.setUpdatedAt(LocalDateTime.now());
                KiotvietOrder ord = orderPort.saveOrder(finalApiOrder);

                String deliveryStatus = ord.getDeliveryStatus();
                boolean isReturnedOrCancelled = "Đang chuyển hoàn".equalsIgnoreCase(deliveryStatus) || "Đã chuyển hoàn".equalsIgnoreCase(deliveryStatus)
                        || "Hủy".equalsIgnoreCase(deliveryStatus) || "Đã hủy".equalsIgnoreCase(deliveryStatus) || "Bị hủy".equalsIgnoreCase(deliveryStatus);

                if (isReturnedOrCancelled) {
                    applyPenalty(finalCustomer, ord);
                    throw new BusinessRuleViolationException("Đơn hàng đã bị hoàn trả hoặc hủy.");
                }

                generateEggsIfNeeded(ord);
                return new SyncDataHolder(ord, finalCustomer);
            });
            currentOrder = holder.order;
            customer = holder.customer;
        } else {
            currentOrder = existingOrderOpt.get();
            if (currentOrder.getCustomerCode() == null || currentOrder.getCustomerCode().trim().isEmpty() || "KHACH_LE".equalsIgnoreCase(currentOrder.getCustomerCode().trim())) {
                throw new BusinessRuleViolationException("Thiếu thông tin khách hàng");
            }

            customer = customerPort.loadByCustomerCode(currentOrder.getCustomerCode().trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Lỗi dữ liệu khách hàng."));

            if ("BANNED".equalsIgnoreCase(customer.getStatus())) {
                throw new BusinessRuleViolationException("Tài khoản bị khóa do vi phạm chính sách");
            }
            if ("TEMP_BANNED".equalsIgnoreCase(customer.getStatus()) && customer.getUnbanAt() != null && LocalDateTime.now().isBefore(customer.getUnbanAt())) {
                throw new BusinessRuleViolationException("Tài khoản của bạn đang bị khóa tạm thời đến ngày " + customer.getUnbanAt() + ".");
            }

            // 1. Đồng bộ lại qua API ngoài transaction nếu cache hết hạn
            currentOrder = syncOrderIfNeeded(currentOrder);

            String deliveryStatus = currentOrder.getDeliveryStatus();
            boolean isReturnedOrCancelled = "Đang chuyển hoàn".equalsIgnoreCase(deliveryStatus) || "Đã chuyển hoàn".equalsIgnoreCase(deliveryStatus)
                    || "Hủy".equalsIgnoreCase(deliveryStatus) || "Đã hủy".equalsIgnoreCase(deliveryStatus) || "Bị hủy".equalsIgnoreCase(deliveryStatus);

            if (isReturnedOrCancelled) {
                List<Egg> orderEggs = eggPort.loadEggsByOrderId(currentOrder.getId());
                boolean hasActiveEggs = orderEggs.stream().anyMatch(e -> !"CANCELLED".equals(e.getStatus()));
                if (hasActiveEggs) {
                    final Customer finalCus = customer;
                    final KiotvietOrder finalOrd = currentOrder;
                    transactionTemplate.executeWithoutResult(status -> {
                        applyPenalty(finalCus, finalOrd);
                    });
                }
                throw new BusinessRuleViolationException("Đơn hàng đã bị hoàn trả hoặc hủy.");
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

        int totalType1 = (int) eggs.stream().filter(e -> e.getEggType() == 1).count();
        int totalType2 = (int) eggs.stream().filter(e -> e.getEggType() == 2).count();

        return SyncOrderResponse.builder()
                .customerName(customer.getCustomerName() != null ? customer.getCustomerName() : "Khách hàng")
                .customerStatus(customer.getStatus())
                .orderId(currentOrder.getId())
                .deliveryStatus(currentOrder.getDeliveryStatus())
                .eggs(eggDtos)
                .totalType1Eggs(totalType1)
                .totalType2Eggs(totalType2)
                .build();
    }

    private void applyPenalty(Customer customer, KiotvietOrder order) {
        List<Egg> orderEggs = eggPort.loadEggsByOrderId(order.getId());
        for (Egg egg : orderEggs) {
            egg.setStatus("CANCELLED");
            eggPort.saveEgg(egg);
        }

        int banDays = configPort.findByKey("BAN_DAY")
                .map(c -> Integer.parseInt(c.getConfigValue()))
                .orElse(7);
        boolean permanentBanEnabled = configPort.findByKey("PERMANENT_BAN")
                .map(c -> Boolean.parseBoolean(c.getConfigValue()))
                .orElse(false);

        customer.setReturnCount(customer.getReturnCount() + 1);
        customer.setSuccessCount(0);
        customer.setEarlyHatchCredits(0);

        String currentStatus = customer.getStatus();
        if ("NORMAL".equalsIgnoreCase(currentStatus) || "NEW".equalsIgnoreCase(currentStatus) || currentStatus == null) {
            customer.setStatus("WARNING");
            customer.setReturnStreak(1);
            customer.setWarningCount(customer.getWarningCount() + 1);
        } else if ("WARNING".equalsIgnoreCase(currentStatus)) {
            customer.setStatus("TEMP_BANNED");
            customer.setReturnStreak(2);
            customer.setUnbanAt(LocalDateTime.now().plusDays(banDays));
        } else if ("TEMP_BANNED".equalsIgnoreCase(currentStatus)) {
            if (permanentBanEnabled) {
                customer.setStatus("BANNED");
                customer.setReturnStreak(3);
            } else {
                customer.setStatus("TEMP_BANNED");
                customer.setReturnStreak(2);
                customer.setUnbanAt(LocalDateTime.now().plusDays(banDays));
            }
        }
        customerPort.saveCustomer(customer);
    }

    @Override
    public KiotvietOrder syncOrderIfNeeded(KiotvietOrder order) {
        if (order.getLastSyncedAt() != null && order.getLastSyncedAt().plusMinutes(1).isAfter(LocalDateTime.now())) {
            return order;
        }

        log.info("Đang đồng bộ lại đơn hàng {} do quá hạn 1 phút cache.", order.getOrderCode());

        KiotvietOrder apiOrder = apiPort.fetchOrderFromKiotviet(order.getOrderCode())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã đơn hàng này, vui lòng thử lại sau."));

        if (apiOrder.getCustomerCode() == null || apiOrder.getCustomerCode().trim().isEmpty() || "KHACH_LE".equalsIgnoreCase(apiOrder.getCustomerCode().trim())) {
            throw new BusinessRuleViolationException("Thiếu thông tin khách hàng");
        }
        
        log.info(apiOrder.getDeliveryStatus());

        return transactionTemplate.execute(status -> {
            KiotvietOrder dbOrder = orderPort.loadByOrderCode(order.getOrderCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại."));

            dbOrder.setDeliveryStatus(apiOrder.getDeliveryStatus());
            if ("Đã giao hàng".equalsIgnoreCase(apiOrder.getDeliveryStatus())) {
                if (dbOrder.getUpdatedAt() == null) {
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

            String deliveryStatus = apiOrder.getDeliveryStatus();
            boolean isReturnedOrCancelledNow = "Đang chuyển hoàn".equalsIgnoreCase(deliveryStatus) || "Đã chuyển hoàn".equalsIgnoreCase(deliveryStatus)
                    || "Hủy".equalsIgnoreCase(deliveryStatus) || "Đã hủy".equalsIgnoreCase(deliveryStatus) || "Bị hủy".equalsIgnoreCase(deliveryStatus);

            if (isReturnedOrCancelledNow) {
                List<Egg> eggs = eggPort.loadEggsByOrderId(dbOrder.getId());
                boolean hasActiveEggs = eggs.stream().anyMatch(e -> !"CANCELLED".equals(e.getStatus()));
                
                if (hasActiveEggs) {
                    Customer cus = customerPort.loadByCustomerCodeForUpdate(dbOrder.getCustomerCode())
                            .orElseThrow(() -> new ResourceNotFoundException("Lỗi dữ liệu khách hàng."));
                    
                    // Gọi hàm applyPenalty. Hàm này sẽ tự động hủy toàn bộ trứng và phạt Customer.
                    applyPenalty(cus, dbOrder); 
                }
            }

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

        List<KiotvietOrderItem> items = order.getOrderItems();
        if (items == null || items.isEmpty()) {
            return;
        }

        List<String> productIds = items.stream()
                .map(KiotvietOrderItem::getKvProductId).collect(Collectors.toList());

        List<ProductEggMapping> mappings = mappingPort.loadMappingsByProductIds(productIds);
        if (mappings.isEmpty()) {
            notificationPort.sendAlert(String.format(
                    "<b>CẢNH BÁO SỰ CỐ: Không có trứng khả dụng</b>\n" +
                    "• Mã đơn hàng: <code>%s</code>\n" +
                    "• Mã khách hàng: <code>%s</code>\n" +
                    "• Chi tiết: Sản phẩm trong đơn chưa được cấu hình liên kết với bể quà.",
                    order.getOrderCode(), order.getCustomerCode()
            ));
            return;
        }

        Customer customer = customerPort.loadByCustomerCodeForUpdate(order.getCustomerCode())
                .orElseThrow(() -> new ResourceNotFoundException("Lỗi dữ liệu khách hàng."));

        boolean isSingleItem = items.size() == 1 && items.get(0).getQuantity() == 1;
        boolean useCredit = false;

        if (isSingleItem && customer.getEarlyHatchCredits() > 0) {
            customer.setEarlyHatchCredits(customer.getEarlyHatchCredits() - 1);
            customerPort.saveCustomer(customer);
            useCredit = true;
        }

        List<Egg> eggsToSave = new ArrayList<>();

        for (KiotvietOrderItem item : items) {
            String productCode = "UNKNOWN";
            try {
                long prodId = Long.parseLong(item.getKvProductId());
                productCode = productPort.findById(prodId)
                        .map(KiotvietProduct::getCode)
                        .orElse("UNKNOWN");
            } catch (NumberFormatException e) {
                // ignore
            }

            final String finalProductCode = productCode;
            List<ProductEggMapping> type1Mappings = mappings.stream()
                    .filter(m -> m.getProductCode() != null 
                            && String.valueOf(m.getProductCode().getKvProductId()).equals(item.getKvProductId())
                            && m.getMappingsType() == 1)
                    .collect(Collectors.toList());
            List<ProductEggMapping> type2Mappings = mappings.stream()
                    .filter(m -> m.getProductCode() != null 
                            && String.valueOf(m.getProductCode().getKvProductId()).equals(item.getKvProductId())
                            && m.getMappingsType() == 2)
                    .collect(Collectors.toList());

            int qty = item.getQuantity();
            for (int i = 0; i < qty; i++) {
                // Draw pool for Egg 1 (Type 1)
                ProductEggMapping mapping1 = drawMappingFromList(type1Mappings);
                if (mapping1 != null) {
                    LocalDateTime hatchAt = null;
                    String initialStatus = "READY_TO_CLAIM";
                    if ("WARNING".equalsIgnoreCase(customer.getStatus())) {
                        hatchAt = LocalDateTime.now().plusDays(3);
                        initialStatus = "HATCHING";
                    }

                    Egg egg1 = Egg.builder()
                            .id(UUID.randomUUID().toString())
                            .order(order)
                            .giftPool(mapping1.getGiftPoolId())
                            .eggType(1)
                            .status(initialStatus)
                            .createdAt(LocalDateTime.now())
                            .hatchAt(hatchAt)
                            .productCode(finalProductCode)
                            .build();
                    eggsToSave.add(egg1);
                }

                // Draw pool for Egg 2 (Type 2)
                ProductEggMapping mapping2 = drawMappingFromList(type2Mappings);
                if (mapping2 != null) {
                    LocalDateTime hatchAt = LocalDateTime.now().plusDays(15);
                    if (useCredit) {
                        hatchAt = hatchAt.minusDays(3);
                    }

                    Egg egg2 = Egg.builder()
                            .id(UUID.randomUUID().toString())
                            .order(order)
                            .giftPool(mapping2.getGiftPoolId())
                            .eggType(2)
                            .status("HATCHING")
                            .createdAt(LocalDateTime.now())
                            .hatchAt(hatchAt)
                            .productCode(finalProductCode)
                            .build();
                    eggsToSave.add(egg2);
                }
            }
        }

        if (!eggsToSave.isEmpty()) {
            eggPort.saveAllEggs(eggsToSave);
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
                String deliveryStatus = egg.getOrder().getDeliveryStatus();
                boolean isDelivered = "Đã giao hàng".equalsIgnoreCase(deliveryStatus) || "Giao thành công".equalsIgnoreCase(deliveryStatus);

                if (!isDelivered) {
                    displayStatus = "WAITING_ORDER_COMPLETION";
                } else {
                    boolean inHatchCooldown = hatchAt != null && LocalDateTime.now().isBefore(hatchAt);
                    if (inHatchCooldown) {
                        displayStatus = "HATCHING";
                    } else {
                        displayStatus = "READY_TO_CLAIM";
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
                    .productCode(egg.getProductCode())
                    .build());
        }
        return result;
    }
}
