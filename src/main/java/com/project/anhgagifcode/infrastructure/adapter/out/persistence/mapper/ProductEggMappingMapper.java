package com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper;

import com.project.anhgagifcode.domain.model.ProductEggMapping;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.ProductEggMappings;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring", 
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {GiftPoolMapper.class, KiotvietProductMapper.class}
)
public interface ProductEggMappingMapper {
    ProductEggMapping toDomain(ProductEggMappings entity);
    ProductEggMappings toEntity(ProductEggMapping domain);
}