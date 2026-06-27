package com.project.anhgagifcode.application.port.in;

import com.project.anhgagifcode.application.port.in.dto.RemoveAccountsFromPoolRequest;

public interface RemoveAccountsFromPoolUseCase {
    void removeAccountsFromPool(RemoveAccountsFromPoolRequest request);
}
