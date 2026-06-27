package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.RemoveAccountsFromPoolUseCase;
import com.project.anhgagifcode.application.port.in.dto.RemoveAccountsFromPoolRequest;
import com.project.anhgagifcode.application.port.out.PoolAccountMappingPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class RemoveAccountsFromPoolService implements RemoveAccountsFromPoolUseCase {

    private final PoolAccountMappingPersistencePort mappingPersistencePort;

    @Override
    @Transactional
    public void removeAccountsFromPool(RemoveAccountsFromPoolRequest request) {
        mappingPersistencePort.removeMappings(request.getPoolId(), request.getAccountIds());
    }
}
