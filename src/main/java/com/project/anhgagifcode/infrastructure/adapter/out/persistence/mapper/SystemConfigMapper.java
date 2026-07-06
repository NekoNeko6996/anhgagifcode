package com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper;

import com.project.anhgagifcode.domain.model.SystemConfig;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.SystemConfigs;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SystemConfigMapper {
    SystemConfig toDomain(SystemConfigs entity);
    SystemConfigs toEntity(SystemConfig domain);
}
