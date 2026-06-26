package com.project.anhgagifcode.infrastructure.adapter.out.persistence;

import com.project.anhgagifcode.application.port.out.KiotvietOrderPersistencePort;
import com.project.anhgagifcode.domain.model.KiotvietOrder;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.KiotvietOrders;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper.KiotvietOrderMapper;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository.KiotvietOrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class KiotvietOrderPersistenceAdapter implements KiotvietOrderPersistencePort {

    private final KiotvietOrderJpaRepository repository;
    private final KiotvietOrderMapper mapper;

    @Override
    public Optional<KiotvietOrder> loadByOrderCode(String orderCode) {
        return repository.findByOrderCode(orderCode).map(mapper::toDomain);
    }

    @Override
    public KiotvietOrder saveOrder(KiotvietOrder order) {
        KiotvietOrders entity = mapper.toEntity(order);
        KiotvietOrders savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}