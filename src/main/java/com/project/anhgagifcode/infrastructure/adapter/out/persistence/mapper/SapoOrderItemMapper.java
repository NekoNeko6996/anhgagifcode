package com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper;

import com.project.anhgagifcode.domain.model.SapoOrderItem;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.SapoOrderItems;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SapoOrderItemMapper {
    
    @Mapping(source = "orderId.id", target = "orderId")
    SapoOrderItem toDomain(SapoOrderItems entity);

    @Mapping(target = "orderId", ignore = true) // Sẽ được set thủ công ở OrderMapper để tránh lỗi vòng lặp
    SapoOrderItems toEntity(SapoOrderItem domain);
}