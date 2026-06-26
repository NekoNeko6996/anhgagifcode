package com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper;

import com.project.anhgagifcode.domain.model.Egg;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.Eggs;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {KiotvietOrderMapper.class, GiftAccountMapper.class, GiftPoolMapper.class})
public interface EggMapper {

    @Mapping(source = "orderId", target = "order") // NetBeans gen SapoOrders thành KiotvietOrders
    @Mapping(source = "accountId", target = "account")
    @Mapping(source = "giftPoolId", target = "giftPool")
    Egg toDomain(Eggs entity);

    @Mapping(source = "order", target = "orderId")
    @Mapping(source = "account", target = "accountId")
    @Mapping(source = "giftPool", target = "giftPoolId")
    @Mapping(target = "eggOpeningLogsCollection", ignore = true)
    Eggs toEntity(Egg domain);
}