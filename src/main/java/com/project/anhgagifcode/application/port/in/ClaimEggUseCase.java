package com.project.anhgagifcode.application.port.in;

import com.project.anhgagifcode.application.port.in.dto.ClaimEggResponse;

public interface ClaimEggUseCase {
    ClaimEggResponse claimEggReward(String eggId, String ipAddress);
}