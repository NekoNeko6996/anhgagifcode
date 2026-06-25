package com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper;

import com.project.anhgagifcode.domain.model.GiftPool;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.GiftPools;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GiftPoolMapper {
    
    GiftPool toDomain(GiftPools entity);

    @Mapping(target = "productEggMappingsCollection", ignore = true)
    @Mapping(target = "eggsCollection", ignore = true)
    @Mapping(target = "poolAccountMappingsCollection", ignore = true)
    GiftPools toEntity(GiftPool domain);
}