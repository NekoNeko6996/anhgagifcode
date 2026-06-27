package com.project.anhgagifcode.application.port.in;

import com.project.anhgagifcode.application.port.in.dto.UpdateGiftAccountRequest;
import com.project.anhgagifcode.application.port.in.dto.GiftAccountDto;

public interface UpdateGiftAccountUseCase {
    GiftAccountDto updateGiftAccount(String id, UpdateGiftAccountRequest request);
}
