package com.project.anhgagifcode.infrastructure.adapter.out.persistence;

import com.project.anhgagifcode.application.port.out.EggOpeningLogPersistencePort;
import com.project.anhgagifcode.domain.model.EggOpeningLog;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.EggOpeningLogs;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper.EggOpeningLogMapper;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository.EggOpeningLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EggOpeningLogPersistenceAdapter implements EggOpeningLogPersistencePort {

    private final EggOpeningLogRepository logRepository;
    private final EggOpeningLogMapper logMapper;

    @Override
    public EggOpeningLog saveLog(EggOpeningLog log) {
        EggOpeningLogs entity = logMapper.toEntity(log);
        EggOpeningLogs savedEntity = logRepository.save(entity);
        return logMapper.toDomain(savedEntity);
    }
}