package com.project.anhgagifcode.infrastructure.adapter.out.persistence;

import com.project.anhgagifcode.application.port.out.SapoOrderPersistencePort;
import com.project.anhgagifcode.domain.model.SapoOrder;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.SapoOrders;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper.SapoOrderMapper;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository.SapoOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SapoOrderPersistenceAdapter implements SapoOrderPersistencePort {

    private final SapoOrderRepository sapoOrderRepository;
    private final SapoOrderMapper sapoOrderMapper;

    @Override
    public Optional<SapoOrder> loadOrderByCode(String orderCode) {
        return sapoOrderRepository.findByOrderCode(orderCode).map(sapoOrderMapper::toDomain);
    }

    @Override
    public SapoOrder saveOrder(SapoOrder order) {
        SapoOrders entity = sapoOrderMapper.toEntity(order);
        SapoOrders savedEntity = sapoOrderRepository.save(entity);
        return sapoOrderMapper.toDomain(savedEntity);
    }
}