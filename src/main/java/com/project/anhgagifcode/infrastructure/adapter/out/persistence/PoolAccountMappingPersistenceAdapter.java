package com.project.anhgagifcode.infrastructure.adapter.out.persistence;

import com.project.anhgagifcode.application.port.out.PoolAccountMappingPersistencePort;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.GiftAccounts;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.GiftPools;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.PoolAccountMappings;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository.GiftAccountJpaRepository;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository.GiftPoolJpaRepository;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository.PoolAccountMappingJpaRepository;
import com.project.anhgagifcode.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PoolAccountMappingPersistenceAdapter implements PoolAccountMappingPersistencePort {

    private final PoolAccountMappingJpaRepository repository;
    private final GiftPoolJpaRepository poolRepository;
    private final GiftAccountJpaRepository accountRepository;

    @Override
    public boolean existsByPoolIdAndAccountId(String poolId, String accountId) {
        return repository.existsByPoolIdIdAndAccountIdId(poolId, accountId);
    }

    @Override
    public void saveMapping(String poolId, String accountId) {
        GiftPools pool = poolRepository.findById(poolId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bể quà này."));
        GiftAccounts account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản quà này."));

        PoolAccountMappings mapping = new PoolAccountMappings();
        mapping.setId(UUID.randomUUID().toString());
        mapping.setPoolId(pool);
        mapping.setAccountId(account);

        repository.save(mapping);
    }

    @Override
    public void saveMappings(String poolId, List<String> accountIds) {
        GiftPools pool = poolRepository.findById(poolId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bể quà này."));

        List<String> existingAccountIds = repository.findExistingAccountIdsInPool(poolId, accountIds);
        List<String> newAccountIds = accountIds.stream()
                .filter(id -> !existingAccountIds.contains(id))
                .toList();

        if (newAccountIds.isEmpty()) {
            return;
        }

        List<GiftAccounts> accounts = accountRepository.findAllById(newAccountIds);
        if (accounts.size() != newAccountIds.size()) {
            throw new ResourceNotFoundException("Một hoặc nhiều tài khoản quà tặng không tồn tại.");
        }

        List<PoolAccountMappings> mappings = accounts.stream()
                .map(account -> {
                    PoolAccountMappings mapping = new PoolAccountMappings();
                    mapping.setId(UUID.randomUUID().toString());
                    mapping.setPoolId(pool);
                    mapping.setAccountId(account);
                    return mapping;
                })
                .toList();

        repository.saveAll(mappings);
    }

    @Override
    @Transactional
    public void removeMappings(String poolId, List<String> accountIds) {
        if (!poolRepository.existsById(poolId)) {
            throw new ResourceNotFoundException("Không tìm thấy bể quà này.");
        }
        repository.deleteByPoolIdAndAccountIds(poolId, accountIds);
    }
}
