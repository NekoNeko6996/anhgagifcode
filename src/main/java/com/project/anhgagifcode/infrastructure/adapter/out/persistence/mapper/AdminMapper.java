package com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper;

import com.project.anhgagifcode.domain.model.Admin;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.Admins;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AdminMapper {
    Admin toDomain(Admins entity);
    Admins toEntity(Admin domain);
}