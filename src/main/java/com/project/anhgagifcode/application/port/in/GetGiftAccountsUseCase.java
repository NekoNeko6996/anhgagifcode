package com.project.anhgagifcode.application.port.in;

import com.project.anhgagifcode.application.port.in.dto.GiftAccountDto;
import java.util.List;

public interface GetGiftAccountsUseCase {
    List<GiftAccountDto> getGiftAccounts();
}
