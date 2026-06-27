package com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper;

import com.project.anhgagifcode.domain.model.KiotvietProduct;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.KiotvietProducts;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring", 
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface KiotvietProductMapper {
    KiotvietProduct toDomain(KiotvietProducts entity);
    KiotvietProducts toEntity(KiotvietProduct domain);
}