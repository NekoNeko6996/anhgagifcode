package com.project.anhgagifcode.infrastructure.adapter.out.persistence;

import com.project.anhgagifcode.application.port.out.EggPersistencePort;
import com.project.anhgagifcode.domain.model.Egg;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.Eggs;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper.EggMapper;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository.EggJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EggPersistenceAdapter implements EggPersistencePort {

    private final EggJpaRepository repository;
    private final EggMapper mapper;

    @Override
    public Egg saveEgg(Egg egg) {
        Eggs savedEntity = repository.save(mapper.toEntity(egg));
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Egg> loadEggForUpdate(String eggId) {
        return repository.findByIdForUpdate(eggId).map(mapper::toDomain);
    }

    @Override
    public List<Egg> loadEggsByOrderId(String orderId) {
        return repository.findByOrderId(orderId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void cancelEggsByOrderId(String orderId) {
        repository.cancelEggsByOrderId(orderId);
    }

    @Override
    public boolean existsByOrderIdAndPoolIdAndEggType(String orderId, String poolId, int eggType) {
        return repository.existsByOrderIdAndGiftPoolIdAndEggType(orderId, poolId, eggType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Egg> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}