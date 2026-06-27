package com.project.anhgagifcode.application.port.in;

import com.project.anhgagifcode.application.port.in.dto.GiftPoolDto;
import com.project.anhgagifcode.application.port.in.dto.UpdateGiftPoolRequest;

public interface UpdateGiftPoolUseCase {
    GiftPoolDto updatePool(String poolId, UpdateGiftPoolRequest request);
}
