package com.project.anhgagifcode.infrastructure.adapter.out.persistence;

import com.project.anhgagifcode.application.port.out.GiftAccountPersistencePort;
import com.project.anhgagifcode.domain.model.GiftAccount;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.GiftAccounts;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper.GiftAccountMapper;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository.GiftAccountJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GiftAccountPersistenceAdapter implements GiftAccountPersistencePort {

    private final GiftAccountJpaRepository repository;
    private final GiftAccountMapper mapper;

    @Override
    public long countAvailableAccountsByPoolId(String poolId) {
        return repository.countAvailableByPoolId(poolId);
    }

    @Override
    public Optional<GiftAccount> pickRandomAvailableAccountForUpdate(String poolId, int offset) {
        // Sử dụng PageRequest để set offset và LIMIT 1 (chỉ lấy 1 dòng)
        Page<GiftAccounts> page = repository.findAvailableAccountForUpdate(poolId, PageRequest.of(offset, 1));
        return page.getContent().stream().findFirst().map(mapper::toDomain);
    }

    @Override
    public void updateAccount(GiftAccount account) {
        repository.save(mapper.toEntity(account));
    }
}