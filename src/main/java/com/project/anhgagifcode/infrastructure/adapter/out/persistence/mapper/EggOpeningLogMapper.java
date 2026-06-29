package com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper;

import com.project.anhgagifcode.domain.model.EggOpeningLog;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.EggOpeningLogs;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.GiftAccounts;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.Eggs;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EggOpeningLogMapper {

    @Mapping(source = "accountId.id", target = "accountId")
    @Mapping(source = "eggId.id", target = "eggId")
    EggOpeningLog toDomain(EggOpeningLogs entity);

    @Mapping(source = "accountId", target = "accountId")
    @Mapping(source = "eggId", target = "eggId")
    EggOpeningLogs toEntity(EggOpeningLog domain);

    default GiftAccounts mapStringToGiftAccounts(String id) {
        if (id == null) return null;
        return new GiftAccounts(id);
    }

    default Eggs mapStringToEggs(String id) {
        if (id == null) return null;
        return new Eggs(id);
    }
}