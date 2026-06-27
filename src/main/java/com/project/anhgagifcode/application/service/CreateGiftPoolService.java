package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.CreateGiftPoolUseCase;
import com.project.anhgagifcode.application.port.in.dto.CreateGiftPoolRequest;
import com.project.anhgagifcode.application.port.in.dto.GiftPoolDto;
import com.project.anhgagifcode.application.port.out.GiftPoolPersistencePort;
import com.project.anhgagifcode.domain.model.GiftPool;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public class CreateGiftPoolService implements CreateGiftPoolUseCase {

    private final GiftPoolPersistencePort giftPoolPersistencePort;

    @Override
    @Transactional
    public GiftPoolDto createPool(CreateGiftPoolRequest request) {
        GiftPool newPool = GiftPool.builder()
                .id(UUID.randomUUID().toString())
                .poolName(request.getPoolName())
                .tier(request.getTier())
                .createdAt(LocalDateTime.now())
                .build();

        GiftPool savedPool = giftPoolPersistencePort.savePool(newPool);

        return GiftPoolDto.builder()
                .id(savedPool.getId())
                .poolName(savedPool.getPoolName())
                .tier(savedPool.getTier())
                .createdAt(savedPool.getCreatedAt())
                .build();
    }
}
