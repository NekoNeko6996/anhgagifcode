package com.project.anhgagifcode.application.port.in;

import com.project.anhgagifcode.application.port.in.dto.UpdateAdminCredentialsRequest;

public interface UpdateAdminCredentialsUseCase {
    void updateCredentials(String currentUsername, UpdateAdminCredentialsRequest request);
}
