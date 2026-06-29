package com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper;

import com.project.anhgagifcode.domain.model.ProductEggMapping;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.ProductEggMappings;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring", 
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {GiftPoolMapper.class, KiotvietProductMapper.class}
)
public interface ProductEggMappingMapper {
    @Mapping(source = "kvProductId", target = "productCode")
    ProductEggMapping toDomain(ProductEggMappings entity);

    @Mapping(source = "productCode", target = "kvProductId")
    ProductEggMappings toEntity(ProductEggMapping domain);
}