package com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper;

import com.project.anhgagifcode.domain.model.SapoOrder;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.SapoOrders;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {SapoOrderItemMapper.class})
public interface SapoOrderMapper {

    @Mapping(source = "sapoOrderItemsCollection", target = "orderItems")
    SapoOrder toDomain(SapoOrders entity);

    @Mapping(source = "orderItems", target = "sapoOrderItemsCollection")
    @Mapping(target = "eggsCollection", ignore = true)
    SapoOrders toEntity(SapoOrder domain);

    // Xử lý móc nối quan hệ 2 chiều cho OrderItem
    @AfterMapping
    default void linkOrderItems(@MappingTarget SapoOrders entity) {
        if (entity.getSapoOrderItemsCollection() != null) {
            entity.getSapoOrderItemsCollection().forEach(item -> item.setOrderId(entity));
        }
    }
}