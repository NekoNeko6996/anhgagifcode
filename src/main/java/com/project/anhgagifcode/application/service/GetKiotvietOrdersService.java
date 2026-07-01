package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.GetKiotvietOrdersUseCase;
import com.project.anhgagifcode.application.port.in.dto.KiotvietOrderDto;
import com.project.anhgagifcode.application.port.in.dto.OrderItemDto;
import com.project.anhgagifcode.application.port.out.KiotvietOrderPersistencePort;
import com.project.anhgagifcode.application.port.out.KiotvietProductPersistencePort;
import com.project.anhgagifcode.domain.model.KiotvietProduct;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GetKiotvietOrdersService implements GetKiotvietOrdersUseCase {

    private final KiotvietOrderPersistencePort orderPersistencePort;
    private final KiotvietProductPersistencePort productPersistencePort;

    @Override
    public List<KiotvietOrderDto> getOrders() {
        List<KiotvietProduct> products = productPersistencePort.findAll();
        Map<Long, KiotvietProduct> productMap = products.stream()
                .filter(p -> p.getKvProductId() != null)
                .collect(Collectors.toMap(KiotvietProduct::getKvProductId, p -> p, (p1, p2) -> p1));

        return orderPersistencePort.findAll().stream()
                .map(o -> {
                    List<OrderItemDto> itemDtos = o.getOrderItems() != null ?
                            o.getOrderItems().stream()
                                    .map(item -> {
                                        Long prodId = null;
                                        try {
                                            if (item.getKvProductId() != null) {
                                                prodId = Long.parseLong(item.getKvProductId());
                                            }
                                        } catch (NumberFormatException ignored) {}

                                        KiotvietProduct product = prodId != null ? productMap.get(prodId) : null;

                                        return OrderItemDto.builder()
                                                .id(item.getId())
                                                .kvProductId(item.getKvProductId())
                                                .code(product != null ? product.getCode() : null)
                                                .name(product != null ? product.getName() : null)
                                                .fullName(product != null ? product.getFullName() : null)
                                                .imageUrl(product != null ? product.getImageUrl() : null)
                                                .quantity(item.getQuantity())
                                                .lastSyncedAt(item.getLastSyncedAt())
                                                .build();
                                    })
                                    .collect(Collectors.toList()) : Collections.emptyList();

                    return KiotvietOrderDto.builder()
                            .id(o.getId())
                            .orderCode(o.getOrderCode())
                            .customerCode(o.getCustomerCode())
                            .deliveryStatus(o.getDeliveryStatus())
                            .lastSyncedAt(o.getLastSyncedAt())
                            .createdAt(o.getCreatedAt())
                            .updatedAt(o.getUpdatedAt())
                            .orderItems(itemDtos)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
