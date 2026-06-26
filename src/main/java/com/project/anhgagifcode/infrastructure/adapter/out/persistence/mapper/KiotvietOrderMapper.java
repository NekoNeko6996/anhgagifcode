package com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper;

import com.project.anhgagifcode.domain.model.KiotvietOrder;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.KiotvietOrders;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {KiotvietOrderItemMapper.class})
public interface KiotvietOrderMapper {

    @Mapping(source = "kiotvietOrderItemsCollection", target = "orderItems")
    @Mapping(source = "customerCode.customerCode", target = "customerCode") // Trích xuất String từ Object Customers do NetBeans gen
    KiotvietOrder toDomain(KiotvietOrders entity);

    @Mapping(source = "orderItems", target = "kiotvietOrderItemsCollection")
    @Mapping(target = "eggsCollection", ignore = true)
    @Mapping(target = "customerCode", ignore = true) // Ignore Object Customers để xử lý riêng tại Repository/Service
    KiotvietOrders toEntity(KiotvietOrder domain);

    @AfterMapping
    default void linkOrderItems(@MappingTarget KiotvietOrders entity) {
        if (entity.getKiotvietOrderItemsCollection() != null) {
            entity.getKiotvietOrderItemsCollection().forEach(item -> item.setOrderId(entity));
        }
    }
}