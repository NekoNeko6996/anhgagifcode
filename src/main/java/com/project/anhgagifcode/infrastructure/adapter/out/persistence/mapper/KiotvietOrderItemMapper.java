package com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper;

import com.project.anhgagifcode.domain.model.KiotvietOrderItem;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.KiotvietOrderItems;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface KiotvietOrderItemMapper {
    
    @Mapping(source = "orderId.id", target = "orderId")
    KiotvietOrderItem toDomain(KiotvietOrderItems entity);

    @Mapping(target = "orderId", ignore = true) // Set thủ công ở KiotvietOrderMapper
    KiotvietOrderItems toEntity(KiotvietOrderItem domain);
}