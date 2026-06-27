package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.GetEggsUseCase;
import com.project.anhgagifcode.application.port.in.dto.EggDto;
import com.project.anhgagifcode.application.port.out.EggPersistencePort;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GetEggsService implements GetEggsUseCase {

    private final EggPersistencePort eggPersistencePort;

    @Override
    public List<EggDto> getEggs() {
        return eggPersistencePort.findAll().stream()
                .map(egg -> {
                    EggDto.OrderSummaryDto orderSummary = egg.getOrder() != null ?
                            EggDto.OrderSummaryDto.builder()
                                    .id(egg.getOrder().getId())
                                    .orderCode(egg.getOrder().getOrderCode())
                                    .deliveryStatus(egg.getOrder().getDeliveryStatus())
                                    .build() : null;

                    EggDto.GiftAccountSummaryDto accountSummary = egg.getAccount() != null ?
                            EggDto.GiftAccountSummaryDto.builder()
                                    .id(egg.getAccount().getId())
                                    .username(egg.getAccount().getUsername())
                                    .platform(egg.getAccount().getPlatform())
                                    .tier(egg.getAccount().getTier())
                                    .status(egg.getAccount().getStatus())
                                    .build() : null;

                    EggDto.GiftPoolSummaryDto poolSummary = egg.getGiftPool() != null ?
                            EggDto.GiftPoolSummaryDto.builder()
                                    .id(egg.getGiftPool().getId())
                                    .poolName(egg.getGiftPool().getPoolName())
                                    .tier(egg.getGiftPool().getTier())
                                    .build() : null;

                    return EggDto.builder()
                            .id(egg.getId())
                            .eggType(egg.getEggType())
                            .status(egg.getStatus())
                            .hatchAt(egg.getHatchAt())
                            .createdAt(egg.getCreatedAt())
                            .updatedAt(egg.getUpdatedAt())
                            .order(orderSummary)
                            .account(accountSummary)
                            .giftPool(poolSummary)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
