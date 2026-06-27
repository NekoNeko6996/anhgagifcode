package com.project.anhgagifcode.application.port.in;

import com.project.anhgagifcode.application.port.in.dto.GiftPoolDetailDto;

public interface GetGiftPoolDetailUseCase {
    GiftPoolDetailDto getPoolDetail(String poolId);
}
