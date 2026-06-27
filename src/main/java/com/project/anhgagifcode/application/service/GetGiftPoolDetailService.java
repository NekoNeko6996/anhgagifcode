package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.GetGiftPoolDetailUseCase;
import com.project.anhgagifcode.application.port.in.dto.GiftAccountDto;
import com.project.anhgagifcode.application.port.in.dto.GiftPoolDetailDto;
import com.project.anhgagifcode.application.port.out.GiftAccountPersistencePort;
import com.project.anhgagifcode.application.port.out.GiftPoolPersistencePort;
import com.project.anhgagifcode.domain.exception.ResourceNotFoundException;
import com.project.anhgagifcode.domain.model.GiftPool;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GetGiftPoolDetailService implements GetGiftPoolDetailUseCase {

    private final GiftPoolPersistencePort giftPoolPersistencePort;
    private final GiftAccountPersistencePort giftAccountPersistencePort;

    @Override
    @Transactional(readOnly = true)
    public GiftPoolDetailDto getPoolDetail(String poolId) {
        GiftPool pool = giftPoolPersistencePort.findById(poolId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bể quà này."));

        List<GiftAccountDto> mappedAccounts = giftAccountPersistencePort.findAccountsByPoolId(poolId).stream()
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

        return GiftPoolDetailDto.builder()
                .id(pool.getId())
                .poolName(pool.getPoolName())
                .tier(pool.getTier())
                .createdAt(pool.getCreatedAt())
                .accounts(mappedAccounts)
                .build();
    }
}
