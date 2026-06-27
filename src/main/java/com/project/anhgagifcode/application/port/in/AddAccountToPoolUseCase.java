package com.project.anhgagifcode.application.port.in;

import com.project.anhgagifcode.application.port.in.dto.AddAccountToPoolRequest;

public interface AddAccountToPoolUseCase {
    void addAccountToPool(AddAccountToPoolRequest request);
}
