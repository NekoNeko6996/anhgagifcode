package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.RemoveGiftPoolUseCase;
import com.project.anhgagifcode.application.port.out.GiftPoolPersistencePort;
import com.project.anhgagifcode.domain.exception.BusinessRuleViolationException;
import com.project.anhgagifcode.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class RemoveGiftPoolService implements RemoveGiftPoolUseCase {

    private final GiftPoolPersistencePort giftPoolPersistencePort;

    @Override
    @Transactional
    public void removePool(String poolId) {
        if (!giftPoolPersistencePort.existsById(poolId)) {
            throw new ResourceNotFoundException("Không tìm thấy bể quà cần xóa.");
        }

        if (giftPoolPersistencePort.hasAssociatedEggs(poolId)) {
            throw new BusinessRuleViolationException("Không thể xóa bể quà này vì đang chứa trứng liên kết.");
        }

        giftPoolPersistencePort.deletePool(poolId);
    }
}
