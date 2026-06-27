package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.GetGiftAccountsUseCase;
import com.project.anhgagifcode.application.port.in.dto.GiftAccountDto;
import com.project.anhgagifcode.application.port.out.GiftAccountPersistencePort;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GetGiftAccountsService implements GetGiftAccountsUseCase {

    private final GiftAccountPersistencePort giftAccountPersistencePort;

    @Override
    public List<GiftAccountDto> getGiftAccounts() {
        return giftAccountPersistencePort.findAll().stream()
                .map(a -> GiftAccountDto.builder()
                        .id(a.getId())
                        .username(a.getUsername())
                        .password(a.getPassword())
                        .status(a.getStatus())
                        .tier(a.getTier())
                        .platform(a.getPlatform())
                        .token(a.getToken())
                        .createdAt(a.getCreatedAt())
                        .assignedAt(a.getAssignedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
