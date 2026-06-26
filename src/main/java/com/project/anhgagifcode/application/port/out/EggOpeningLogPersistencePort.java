package com.project.anhgagifcode.application.port.out;

import com.project.anhgagifcode.domain.model.EggOpeningLog;

public interface EggOpeningLogPersistencePort {
    
    // Ghi log vào DB
    void saveLog(EggOpeningLog log);
}