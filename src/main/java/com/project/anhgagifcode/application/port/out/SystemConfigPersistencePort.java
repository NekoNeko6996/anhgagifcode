package com.project.anhgagifcode.application.port.out;

import com.project.anhgagifcode.domain.model.SystemConfig;
import java.util.Optional;
import java.util.List;

public interface SystemConfigPersistencePort {
    Optional<SystemConfig> findByKey(String key);
    SystemConfig save(SystemConfig config);
    List<SystemConfig> findAll();
}
