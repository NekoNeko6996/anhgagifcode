package com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper;

import com.project.anhgagifcode.domain.model.Customer;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.Customers;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    Customer toDomain(Customers entity);

    @Mapping(target = "kiotvietOrdersCollection", ignore = true) // Ignore Collection do NetBeans tự sinh
    Customers toEntity(Customer domain);
}