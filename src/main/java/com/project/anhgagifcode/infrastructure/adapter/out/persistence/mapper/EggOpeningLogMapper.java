package com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper;

import com.project.anhgagifcode.domain.model.EggOpeningLog;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.EggOpeningLogs;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EggOpeningLogMapper {

    @Mapping(source = "accountId.id", target = "accountId")
    @Mapping(source = "eggId.id", target = "eggId")
    EggOpeningLog toDomain(EggOpeningLogs entity);

    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "eggId", ignore = true)
    EggOpeningLogs toEntity(EggOpeningLog domain);
}