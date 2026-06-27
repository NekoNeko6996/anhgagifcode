package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.AddAccountsToPoolUseCase;
import com.project.anhgagifcode.application.port.in.dto.AddAccountsToPoolRequest;
import com.project.anhgagifcode.application.port.out.PoolAccountMappingPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class AddAccountsToPoolService implements AddAccountsToPoolUseCase {

    private final PoolAccountMappingPersistencePort mappingPersistencePort;

    @Override
    @Transactional
    public void addAccountsToPool(AddAccountsToPoolRequest request) {
        mappingPersistencePort.saveMappings(request.getPoolId(), request.getAccountIds());
    }
}
