package com.project.anhgagifcode.application.port.in;

import java.time.LocalDateTime;

public interface UpdateEggHatchTimeUseCase {
    void updateHatchTime(String eggId, LocalDateTime hatchAt);
}
