package com.project.anhgagifcode.infrastructure.adapter.out.persistence;

import com.project.anhgagifcode.application.port.out.SystemConfigPersistencePort;
import com.project.anhgagifcode.domain.model.SystemConfig;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper.SystemConfigMapper;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository.SystemConfigJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SystemConfigPersistenceAdapter implements SystemConfigPersistencePort {

    private final SystemConfigJpaRepository repository;
    private final SystemConfigMapper mapper;

    @Override
    public Optional<SystemConfig> findByKey(String key) {
        return repository.findById(key).map(mapper::toDomain);
    }

    @Override
    public SystemConfig save(SystemConfig config) {
        return mapper.toDomain(repository.save(mapper.toEntity(config)));
    }

    @Override
    public List<SystemConfig> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
