package com.project.anhgagifcode.infrastructure.adapter.in.web;

import com.project.anhgagifcode.application.port.in.AdminAuthUseCase;
import com.project.anhgagifcode.infrastructure.adapter.in.web.dto.AdminLoginRequest;
import com.project.anhgagifcode.infrastructure.adapter.in.web.dto.AdminLoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
@Tag(name = "Admin - Xác Thực", description = "API Đăng nhập dành cho Quản trị viên")
public class AdminAuthController {

    private final AdminAuthUseCase adminAuthUseCase;

    @Operation(summary = "Đăng nhập Admin", description = "Xác thực bằng Username/Password và trả về JWT Token.")
    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        AdminLoginResponse response = adminAuthUseCase.login(request);
        return ResponseEntity.ok(response);
    }
}