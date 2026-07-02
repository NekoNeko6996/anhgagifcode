package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.GetEarlyHatchEligibleUseCase;
import com.project.anhgagifcode.application.port.in.dto.EarlyHatchGroupDto;
import com.project.anhgagifcode.application.port.in.dto.EggDetailDto;
import com.project.anhgagifcode.application.port.in.dto.OrderGroupDto;
import com.project.anhgagifcode.application.port.out.CustomerPersistencePort;
import com.project.anhgagifcode.application.port.out.EggPersistencePort;
import com.project.anhgagifcode.application.port.out.KiotvietProductPersistencePort;
import com.project.anhgagifcode.domain.model.Customer;
import com.project.anhgagifcode.domain.model.Egg;
import com.project.anhgagifcode.domain.model.KiotvietOrder;
import com.project.anhgagifcode.domain.model.KiotvietOrderItem;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GetEarlyHatchEligibleService implements GetEarlyHatchEligibleUseCase {

    private final EggPersistencePort eggPort;
    private final CustomerPersistencePort customerPort;
    private final KiotvietProductPersistencePort productPort;

    @Override
    public List<EarlyHatchGroupDto> getEligibleItems() {
        List<Egg> eggs = eggPort.findAll();
        
        // 1. Lọc ra các trứng đang ấp (hatchAt ở tương lai)
        List<Egg> hatchingEggs = eggs.stream()
                .filter(egg -> egg.getHatchAt() != null && LocalDateTime.now().isBefore(egg.getHatchAt()))
                .filter(egg -> !"CLAIMED".equals(egg.getStatus()) && !"CANCELLED".equals(egg.getStatus()))
                .collect(Collectors.toList());

        if (hatchingEggs.isEmpty()) {
            return Collections.emptyList();
        }

        // Cache customer để tối ưu hóa truy vấn
        Map<String, Customer> customerCache = new HashMap<>();
        
        // Group theo Customer Code -> Map<Order Code -> List<Egg>>
        Map<String, Map<String, List<Egg>>> groupedData = new HashMap<>();

        for (Egg egg : hatchingEggs) {
            KiotvietOrder order = egg.getOrder();
            if (order == null || order.getCustomerCode() == null) {
                continue;
            }

            // Kiểm tra số lượng sản phẩm trong đơn (phải > 1 SKU hoặc 1 SKU có số lượng > 1)
            List<KiotvietOrderItem> items = order.getOrderItems();
            if (items == null || items.isEmpty()) {
                continue;
            }
            boolean isMultiItem = items.size() > 1 || (items.size() == 1 && items.get(0).getQuantity() > 1);
            if (!isMultiItem) {
                continue;
            }

            String customerCode = order.getCustomerCode();
            Customer customer = customerCache.computeIfAbsent(customerCode, code -> 
                customerPort.loadByCustomerCode(code).orElse(null)
            );

            // Chỉ hiển thị các khách hàng có early_hatch_credits > 0
            if (customer == null || customer.getEarlyHatchCredits() <= 0) {
                continue;
            }

            groupedData.computeIfAbsent(customerCode, k -> new HashMap<>())
                       .computeIfAbsent(order.getId(), k -> new ArrayList<>())
                       .add(egg);
        }

        List<EarlyHatchGroupDto> result = new ArrayList<>();

        for (Map.Entry<String, Map<String, List<Egg>>> customerEntry : groupedData.entrySet()) {
            String customerCode = customerEntry.getKey();
            Customer customer = customerCache.get(customerCode);

            List<OrderGroupDto> orderDtos = new ArrayList<>();
            for (Map.Entry<String, List<Egg>> orderEntry : customerEntry.getValue().entrySet()) {
                List<Egg> orderEggs = orderEntry.getValue();
                KiotvietOrder order = orderEggs.get(0).getOrder();

                // Tạo chuỗi mô tả SKU từ database sản phẩm
                String skuDetails = order.getOrderItems().stream()
                        .map(item -> {
                            try {
                                long prodId = Long.parseLong(item.getKvProductId());
                                return productPort.findById(prodId)
                                        .map(p -> p.getCode() + " (SL: " + item.getQuantity() + ")")
                                        .orElse("ID: " + item.getKvProductId() + " (SL: " + item.getQuantity() + ")");
                            } catch (NumberFormatException e) {
                                return "ID: " + item.getKvProductId() + " (SL: " + item.getQuantity() + ")";
                            }
                        })
                        .collect(Collectors.joining(", "));

                List<EggDetailDto> eggDtos = orderEggs.stream()
                        .map(egg -> EggDetailDto.builder()
                                .eggId(egg.getId())
                                .eggType(egg.getEggType())
                                .hatchAt(egg.getHatchAt())
                                .build())
                        .sorted(Comparator.comparingInt(EggDetailDto::getEggType))
                        .collect(Collectors.toList());

                orderDtos.add(OrderGroupDto.builder()
                        .orderId(order.getId())
                        .orderCode(order.getOrderCode())
                        .skuDetails(skuDetails)
                        .eggs(eggDtos)
                        .build());
            }

            result.add(EarlyHatchGroupDto.builder()
                    .customerCode(customerCode)
                    .successCount(customer.getSuccessCount())
                    .earlyHatchCredits(customer.getEarlyHatchCredits())
                    .orders(orderDtos)
                    .build());
        }

        return result;
    }
}
