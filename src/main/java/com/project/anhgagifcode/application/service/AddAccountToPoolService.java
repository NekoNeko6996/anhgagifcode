package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.AddAccountToPoolUseCase;
import com.project.anhgagifcode.application.port.in.dto.AddAccountToPoolRequest;
import com.project.anhgagifcode.application.port.out.PoolAccountMappingPersistencePort;
import com.project.anhgagifcode.domain.exception.BusinessRuleViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class AddAccountToPoolService implements AddAccountToPoolUseCase {

    private final PoolAccountMappingPersistencePort mappingPersistencePort;

    @Override
    @Transactional
    public void addAccountToPool(AddAccountToPoolRequest request) {
        boolean exists = mappingPersistencePort.existsByPoolIdAndAccountId(request.getPoolId(), request.getAccountId());
        if (exists) {
            throw new BusinessRuleViolationException("Tài khoản này đã được gán vào bể quà này trước đó.");
        }

        mappingPersistencePort.saveMapping(request.getPoolId(), request.getAccountId());
    }
}
