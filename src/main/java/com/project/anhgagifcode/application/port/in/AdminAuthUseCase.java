package com.project.anhgagifcode.application.port.in;

import com.project.anhgagifcode.infrastructure.adapter.in.web.dto.AdminLoginRequest;
import com.project.anhgagifcode.infrastructure.adapter.in.web.dto.AdminLoginResponse;

public interface AdminAuthUseCase {
    AdminLoginResponse login(AdminLoginRequest request);
}