package com.project.anhgagifcode.application.port.out;

import com.project.anhgagifcode.domain.model.EggOpeningLog;

public interface EggOpeningLogPersistencePort {
    // Lưu vết ai đã mở trứng (Người dùng tự mở, hoặc Admin ép mở)
    EggOpeningLog saveLog(EggOpeningLog log);
}