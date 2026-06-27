package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.UpdateGiftPoolUseCase;
import com.project.anhgagifcode.application.port.in.dto.GiftPoolDto;
import com.project.anhgagifcode.application.port.in.dto.UpdateGiftPoolRequest;
import com.project.anhgagifcode.application.port.out.GiftPoolPersistencePort;
import com.project.anhgagifcode.domain.exception.ResourceNotFoundException;
import com.project.anhgagifcode.domain.model.GiftPool;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class UpdateGiftPoolService implements UpdateGiftPoolUseCase {

    private final GiftPoolPersistencePort giftPoolPersistencePort;

    @Override
    @Transactional
    public GiftPoolDto updatePool(String poolId, UpdateGiftPoolRequest request) {
        GiftPool pool = giftPoolPersistencePort.findById(poolId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bể quà cần cập nhật."));

        pool.setPoolName(request.getPoolName());
        pool.setTier(request.getTier());

        GiftPool updatedPool = giftPoolPersistencePort.savePool(pool);

        return GiftPoolDto.builder()
                .id(updatedPool.getId())
                .poolName(updatedPool.getPoolName())
                .tier(updatedPool.getTier())
                .createdAt(updatedPool.getCreatedAt())
                .build();
    }
}
