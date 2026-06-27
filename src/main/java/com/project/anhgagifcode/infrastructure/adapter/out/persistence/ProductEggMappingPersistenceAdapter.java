package com.project.anhgagifcode.infrastructure.adapter.out.persistence;

import com.project.anhgagifcode.application.port.out.ProductEggMappingPersistencePort;
import com.project.anhgagifcode.domain.model.ProductEggMapping;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper.ProductEggMappingMapper;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository.ProductEggMappingJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductEggMappingPersistenceAdapter implements ProductEggMappingPersistencePort {

    private final ProductEggMappingJpaRepository repository;
    private final ProductEggMappingMapper mapper;

    @Override
    public List<ProductEggMapping> loadMappingsByProductIds(List<String> kvProductIds) {
        return repository.findByKvProductIdIn(kvProductIds)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductEggMapping> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}