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
        java.util.List<String> candidateCodes = new java.util.ArrayList<>();
        candidateCodes.add(orderCode);

        // Nếu mã không giống mã đầy đủ (không chứa '_' và không bắt đầu bằng các tiền tố quen thuộc), thêm tiền tố tìm kiếm trong DB
        boolean looksLikeFullCode = orderCode.contains("_")
                || orderCode.startsWith("HD")
                || orderCode.startsWith("DH")
                || orderCode.startsWith("OD");

        if (!looksLikeFullCode) {
            java.util.List<String> dbPrefixes = repository.findDistinctPrefixes();
            if (dbPrefixes == null) {
                dbPrefixes = java.util.Collections.emptyList();
            }
            java.util.List<String> defaultPrefixes = java.util.List.of("HDTTS", "HDSPE", "DHTTS", "DHSPE", "HD", "DH", "OD");
            java.util.List<String> allPrefixes = new java.util.ArrayList<>(dbPrefixes);
            for (String def : defaultPrefixes) {
                if (!allPrefixes.contains(def)) {
                    allPrefixes.add(def);
                }
            }
            for (String prefix : allPrefixes) {
                candidateCodes.add(prefix + "_" + orderCode);
                candidateCodes.add(prefix + orderCode);
            }
        }

        List<KiotvietOrders> orders = repository.findByOrderCodeIn(candidateCodes);
        if (orders.isEmpty()) {
            return Optional.empty();
        }
        return orders.stream()
                .filter(o -> o.getOrderCode().equalsIgnoreCase(orderCode))
                .findFirst()
                .or(() -> orders.stream().findFirst())
                .map(mapper::toDomain);
    }

    @Override
    public List<String> findDistinctPrefixes() {
        return repository.findDistinctPrefixes();
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

    @Override
    @Transactional(readOnly = true)
    public List<KiotvietOrder> findByCustomerCode(String customerCode) {
        return repository.findByCustomerCode(customerCode).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
