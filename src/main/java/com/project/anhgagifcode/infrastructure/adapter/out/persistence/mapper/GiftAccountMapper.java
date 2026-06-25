package com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper;

import com.project.anhgagifcode.domain.model.GiftAccount;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.GiftAccounts;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GiftAccountMapper {
    
    @Mapping(source = "platfrom", target = "platform")
    GiftAccount toDomain(GiftAccounts entity);

    @Mapping(source = "platform", target = "platfrom")
    @Mapping(target = "eggs", ignore = true)
    @Mapping(target = "poolAccountMappingsCollection", ignore = true)
    @Mapping(target = "eggOpeningLogsCollection", ignore = true)
    GiftAccounts toEntity(GiftAccount domain);
}