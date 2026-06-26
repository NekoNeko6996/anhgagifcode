package com.project.anhgagifcode.infrastructure.adapter.out.persistence;

import com.project.anhgagifcode.application.port.out.EggOpeningLogPersistencePort;
import com.project.anhgagifcode.domain.model.EggOpeningLog;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper.EggOpeningLogMapper;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository.EggOpeningLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EggOpeningLogPersistenceAdapter implements EggOpeningLogPersistencePort {

    private final EggOpeningLogJpaRepository repository;
    private final EggOpeningLogMapper mapper;

    @Override
    public void saveLog(EggOpeningLog log) {
        repository.save(mapper.toEntity(log));
    }
}