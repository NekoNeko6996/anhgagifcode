package com.project.anhgagifcode.infrastructure.adapter.out.persistence;

import com.project.anhgagifcode.application.port.out.ProductEggMappingPersistencePort;
import com.project.anhgagifcode.domain.model.ProductEggMapping;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.GiftPools;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.KiotvietProducts;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.ProductEggMappings;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper.ProductEggMappingMapper;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository.GiftPoolJpaRepository;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository.KiotvietProductJpaRepository;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository.ProductEggMappingJpaRepository;
import com.project.anhgagifcode.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductEggMappingPersistenceAdapter implements ProductEggMappingPersistencePort {

    private final ProductEggMappingJpaRepository repository;
    private final ProductEggMappingMapper mapper;
    private final KiotvietProductJpaRepository productRepository;
    private final GiftPoolJpaRepository poolRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductEggMapping> loadMappingsByProductIds(List<String> kvProductIds) {
        if (kvProductIds == null || kvProductIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        List<Long> longIds = kvProductIds.stream()
                .filter(id -> id != null && !id.trim().isEmpty())
                .map(id -> {
                    try {
                        return Long.parseLong(id.trim());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        if (longIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        return repository.findByKvProductIdIn(longIds)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductEggMapping> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductEggMapping> findByKvProductId(Long kvProductId) {
        return repository.findByProductId(kvProductId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductEggMapping> findById(String id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void saveMapping(Long kvProductId, String poolId, double rate) {
        KiotvietProducts product = productRepository.findById(kvProductId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm Kiotviet này."));
        GiftPools pool = poolRepository.findById(poolId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bể quà này."));

        ProductEggMappings entity = new ProductEggMappings();
        entity.setId(UUID.randomUUID().toString());
        entity.setKvProductId(product);
        entity.setGiftPoolId(pool);
        entity.setEggTier(pool.getTier());
        entity.setRate(rate);
        entity.setCreatedAt(new java.util.Date());
        entity.setUpdatedAt(new java.util.Date());

        repository.save(entity);
    }

    @Override
    @Transactional
    public void updateMappingRate(String mappingId, double rate) {
        ProductEggMappings entity = repository.findById(mappingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy liên kết."));
        entity.setRate(rate);
        entity.setUpdatedAt(new java.util.Date());
        repository.save(entity);
    }

    @Override
    @Transactional
    public void deleteMapping(String mappingId) {
        if (!repository.existsById(mappingId)) {
            throw new ResourceNotFoundException("Không tìm thấy liên kết này.");
        }
        repository.deleteById(mappingId);
    }

    @Override
    @Transactional
    public void deleteMappings(List<String> mappingIds) {
        if (mappingIds == null || mappingIds.isEmpty()) {
            return;
        }
        repository.deleteAllById(mappingIds);
    }
}