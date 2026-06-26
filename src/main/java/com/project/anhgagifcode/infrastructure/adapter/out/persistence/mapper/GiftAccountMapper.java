package com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper;

import com.project.anhgagifcode.domain.model.GiftAccount;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.GiftAccounts;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GiftAccountMapper {
    GiftAccount toDomain(GiftAccounts entity);
    GiftAccounts toEntity(GiftAccount domain);
}