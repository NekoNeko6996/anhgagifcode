package com.project.anhgagifcode.infrastructure.adapter.out.persistence;

import com.project.anhgagifcode.application.port.out.EggPersistencePort;
import com.project.anhgagifcode.domain.model.Egg;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.Eggs;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.SapoOrders;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper.EggMapper;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository.EggRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EggPersistenceAdapter implements EggPersistencePort {

    private final EggRepository eggRepository;
    private final EggMapper eggMapper;

    @Override
    public Optional<Egg> loadEggByIdWithLock(String id) {
        return eggRepository.findByIdWithLock(id).map(eggMapper::toDomain);
    }

    @Override
    public List<Egg> loadEggsByOrderId(String orderId) {
        return eggRepository.findByOrderId(new SapoOrders(orderId))
                .stream()
                .map(eggMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByOrderIdAndEggType(String orderId, int eggType) {
        return eggRepository.existsByOrderIdAndEggType(new SapoOrders(orderId), eggType);
    }

    @Override
    public Egg saveEgg(Egg egg) {
        Eggs entity = eggMapper.toEntity(egg);
        Eggs savedEntity = eggRepository.save(entity);
        return eggMapper.toDomain(savedEntity);
    }

    @Override
    public void cancelEggsByOrderId(String orderId) {
        List<Eggs> eggs = eggRepository.findByOrderId(new SapoOrders(orderId));
        if (!eggs.isEmpty()) {
            eggs.forEach(egg -> egg.setStatus("CANCELLED"));
            eggRepository.saveAll(eggs);
        }
    }
}