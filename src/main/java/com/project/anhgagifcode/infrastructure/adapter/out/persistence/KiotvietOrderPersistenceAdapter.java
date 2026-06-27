package com.project.anhgagifcode.infrastructure.adapter.out.persistence;

import com.project.anhgagifcode.application.port.out.KiotvietOrderPersistencePort;
import com.project.anhgagifcode.domain.model.KiotvietOrder;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.KiotvietOrders;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper.KiotvietOrderMapper;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository.CustomerJpaRepository;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository.KiotvietOrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class KiotvietOrderPersistenceAdapter implements KiotvietOrderPersistencePort {

    private final KiotvietOrderJpaRepository repository;
    private final KiotvietOrderMapper mapper;
    private final CustomerJpaRepository customerJpaRepository;

    @Override
    public Optional<KiotvietOrder> loadByOrderCode(String orderCode) {
        return repository.findByOrderCode(orderCode).map(mapper::toDomain);
    }

    @Override
    public KiotvietOrder saveOrder(KiotvietOrder order) {
        KiotvietOrders entity = mapper.toEntity(order);

        if (order.getCustomerCode() != null) {
            customerJpaRepository.findByCustomerCode(order.getCustomerCode())
                    .ifPresent(entity::setCustomerCode);
        }

        KiotvietOrders savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KiotvietOrder> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
