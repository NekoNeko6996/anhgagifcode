package com.project.anhgagifcode.application.port.in;

import com.project.anhgagifcode.application.port.in.dto.DeleteGiftAccountsRequest;

public interface DeleteGiftAccountsUseCase {
    void deleteAccounts(DeleteGiftAccountsRequest request);
}
