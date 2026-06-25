package com.project.anhgagifcode.infrastructure.adapter.out.persistence;

import com.project.anhgagifcode.application.port.out.GiftAccountPersistencePort;
import com.project.anhgagifcode.domain.model.GiftAccount;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.GiftAccounts;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper.GiftAccountMapper;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository.GiftAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GiftAccountPersistenceAdapter implements GiftAccountPersistencePort {

    private final GiftAccountRepository giftAccountRepository;
    private final GiftAccountMapper giftAccountMapper;

    @Override
    public long countAvailableAccounts(String poolId) {
        return giftAccountRepository.countAvailableByPoolId(poolId);
    }

    @Override
    public Optional<GiftAccount> loadAvailableAccountWithLock(String poolId, int offset) {
        List<GiftAccounts> accounts = giftAccountRepository.findAvailableByPoolIdWithLock(poolId, PageRequest.of(offset, 1));
        if (accounts.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(giftAccountMapper.toDomain(accounts.get(0)));
    }

    @Override
    public GiftAccount saveAccount(GiftAccount account) {
        GiftAccounts entity = giftAccountMapper.toEntity(account);
        GiftAccounts savedEntity = giftAccountRepository.save(entity);
        return giftAccountMapper.toDomain(savedEntity);
    }
}