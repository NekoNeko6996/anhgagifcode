package com.project.anhgagifcode.application.port.in;

import com.project.anhgagifcode.application.port.in.dto.GiftPoolDto;
import java.util.List;

public interface GetGiftPoolsUseCase {
    List<GiftPoolDto> getGiftPools();
}
