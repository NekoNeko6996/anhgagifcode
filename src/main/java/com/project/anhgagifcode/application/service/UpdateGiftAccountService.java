package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.UpdateGiftAccountUseCase;
import com.project.anhgagifcode.application.port.in.dto.GiftAccountDto;
import com.project.anhgagifcode.application.port.in.dto.UpdateGiftAccountRequest;
import com.project.anhgagifcode.application.port.out.GiftAccountPersistencePort;
import com.project.anhgagifcode.domain.exception.ResourceNotFoundException;
import com.project.anhgagifcode.domain.model.GiftAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class UpdateGiftAccountService implements UpdateGiftAccountUseCase {

    private final GiftAccountPersistencePort giftAccountPersistencePort;

    @Override
    @Transactional
    public GiftAccountDto updateGiftAccount(String id, UpdateGiftAccountRequest request) {
        GiftAccount account = giftAccountPersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản quà tặng này."));

        account.setUsername(request.getUsername());
        account.setPassword(request.getPassword());
        account.setPlatform(request.getPlatform());
        account.setStatus(request.getStatus());
        account.setToken(request.getToken());

        GiftAccount savedAccount = giftAccountPersistencePort.save(account);

        return GiftAccountDto.builder()
                .id(savedAccount.getId())
                .username(savedAccount.getUsername())
                .password(savedAccount.getPassword())
                .platform(savedAccount.getPlatform())
                .status(savedAccount.getStatus())
                .token(savedAccount.getToken())
                .createdAt(savedAccount.getCreatedAt())
                .assignedAt(savedAccount.getAssignedAt())
                .build();
    }
}
