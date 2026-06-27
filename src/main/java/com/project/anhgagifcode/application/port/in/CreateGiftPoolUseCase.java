package com.project.anhgagifcode.application.port.in;

import com.project.anhgagifcode.application.port.in.dto.CreateGiftPoolRequest;
import com.project.anhgagifcode.application.port.in.dto.GiftPoolDto;

public interface CreateGiftPoolUseCase {
    GiftPoolDto createPool(CreateGiftPoolRequest request);
}
