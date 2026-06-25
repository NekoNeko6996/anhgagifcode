package com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper;

import com.project.anhgagifcode.domain.model.ProductEggMapping;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.ProductEggMappings;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {GiftPoolMapper.class})
public interface ProductEggMappingMapper {

    @Mapping(source = "giftPoolId", target = "giftPool")
    ProductEggMapping toDomain(ProductEggMappings entity);

    @Mapping(source = "giftPool", target = "giftPoolId")
    ProductEggMappings toEntity(ProductEggMapping domain);
}