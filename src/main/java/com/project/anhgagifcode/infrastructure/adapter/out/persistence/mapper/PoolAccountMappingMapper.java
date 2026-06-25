package com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper;

import com.project.anhgagifcode.domain.model.PoolAccountMapping;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.PoolAccountMappings;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PoolAccountMappingMapper {

    @Mapping(source = "accountId.id", target = "accountId")
    @Mapping(source = "poolId.id", target = "poolId")
    PoolAccountMapping toDomain(PoolAccountMappings entity);

    @Mapping(target = "accountId", ignore = true) // Cấu hình ignore đối tượng rườm rà, khi save sẽ dùng logic repository
    @Mapping(target = "poolId", ignore = true)
    PoolAccountMappings toEntity(PoolAccountMapping domain);
}