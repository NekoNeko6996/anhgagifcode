package com.project.anhgagifcode.infrastructure.adapter.out.persistence;

import com.project.anhgagifcode.application.port.out.GiftPoolPersistencePort;
import com.project.anhgagifcode.domain.model.GiftPool;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.GiftPools;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper.GiftPoolMapper;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository.EggJpaRepository;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository.GiftPoolJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GiftPoolPersistenceAdapter implements GiftPoolPersistencePort {

    private final GiftPoolJpaRepository repository;
    private final GiftPoolMapper mapper;
    private final EggJpaRepository eggRepository;

    @Override
    @Transactional(readOnly = true)
    public List<GiftPool> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<GiftPool> findById(String id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public GiftPool savePool(GiftPool pool) {
        GiftPools entity = mapper.toEntity(pool);
        GiftPools savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public void deletePool(String id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return repository.existsById(id);
    }

    @Override
    public boolean hasAssociatedEggs(String id) {
        return eggRepository.existsByGiftPoolIdId(id);
    }
}
