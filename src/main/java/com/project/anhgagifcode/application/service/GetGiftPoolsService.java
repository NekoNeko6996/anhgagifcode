package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.GetGiftPoolsUseCase;
import com.project.anhgagifcode.application.port.in.dto.GiftPoolDto;
import com.project.anhgagifcode.application.port.out.GiftPoolPersistencePort;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GetGiftPoolsService implements GetGiftPoolsUseCase {

    private final GiftPoolPersistencePort giftPoolPersistencePort;

    @Override
    public List<GiftPoolDto> getGiftPools() {
        return giftPoolPersistencePort.findAll().stream()
                .map(p -> GiftPoolDto.builder()
                        .id(p.getId())
                        .poolName(p.getPoolName())
                        .tier(p.getTier())
                        .createdAt(p.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
