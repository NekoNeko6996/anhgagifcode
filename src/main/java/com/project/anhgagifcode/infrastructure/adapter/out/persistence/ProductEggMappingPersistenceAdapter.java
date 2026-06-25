package com.project.anhgagifcode.infrastructure.adapter.out.persistence;

import com.project.anhgagifcode.application.port.out.ProductEggMappingPersistencePort;
import com.project.anhgagifcode.domain.model.ProductEggMapping;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.ProductEggMappings;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper.ProductEggMappingMapper;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository.ProductEggMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductEggMappingPersistenceAdapter implements ProductEggMappingPersistencePort {

    private final ProductEggMappingRepository mappingRepository;
    private final ProductEggMappingMapper mappingMapper;

    @Override
    public Optional<ProductEggMapping> loadHighestTierMapping(List<String> sapoProductIds) {
        return mappingRepository.findFirstBySapoProductIdInOrderByEggTierDesc(sapoProductIds)
                .map(mappingMapper::toDomain);
    }

    @Override
    public boolean existsMapping(String sapoProductId, int eggType) {
        return mappingRepository.existsBySapoProductIdAndEggType(sapoProductId, eggType);
    }

    @Override
    public ProductEggMapping saveMapping(ProductEggMapping mapping) {
        ProductEggMappings entity = mappingMapper.toEntity(mapping);
        ProductEggMappings savedEntity = mappingRepository.save(entity);
        return mappingMapper.toDomain(savedEntity);
    }

    @Override
    public void deleteMapping(String id) {
        mappingRepository.deleteById(id);
    }
}