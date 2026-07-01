package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.UpdateEggHatchTimeUseCase;
import com.project.anhgagifcode.application.port.out.EggPersistencePort;
import com.project.anhgagifcode.domain.exception.ResourceNotFoundException;
import com.project.anhgagifcode.domain.model.Egg;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class UpdateEggHatchTimeService implements UpdateEggHatchTimeUseCase {

    private final EggPersistencePort eggPersistencePort;

    @Override
    @Transactional
    public void updateHatchTime(String eggId, LocalDateTime hatchAt) {
        Egg egg = eggPersistencePort.findById(eggId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trứng hợp lệ."));
        egg.setHatchAt(hatchAt);
        eggPersistencePort.saveEgg(egg);
    }
}
