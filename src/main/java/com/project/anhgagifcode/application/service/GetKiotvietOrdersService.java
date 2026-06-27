package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.GetKiotvietOrdersUseCase;
import com.project.anhgagifcode.application.port.in.dto.KiotvietOrderDto;
import com.project.anhgagifcode.application.port.in.dto.OrderItemDto;
import com.project.anhgagifcode.application.port.out.KiotvietOrderPersistencePort;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GetKiotvietOrdersService implements GetKiotvietOrdersUseCase {

    private final KiotvietOrderPersistencePort orderPersistencePort;

    @Override
    public List<KiotvietOrderDto> getOrders() {
        return orderPersistencePort.findAll().stream()
                .map(o -> {
                    List<OrderItemDto> itemDtos = o.getOrderItems() != null ?
                            o.getOrderItems().stream()
                                    .map(item -> OrderItemDto.builder()
                                            .id(item.getId())
                                            .kvProductId(item.getKvProductId())
                                            .quantity(item.getQuantity())
                                            .lastSyncedAt(item.getLastSyncedAt())
                                            .build())
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
