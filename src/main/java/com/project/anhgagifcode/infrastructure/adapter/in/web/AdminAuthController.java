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
@Tag(name = "Admin - Xác Thực", description = "API Đăng nhập và quản lý thông tin tài khoản dành cho Quản trị viên")
public class AdminAuthController {

    private final AdminAuthUseCase adminAuthUseCase;
    private final com.project.anhgagifcode.application.port.in.UpdateAdminCredentialsUseCase updateAdminCredentialsUseCase;

    @Operation(summary = "Đăng nhập Admin", description = "Xác thực bằng Username/Password và trả về JWT Token.")
    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        AdminLoginResponse response = adminAuthUseCase.login(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/credentials")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Đổi thông tin đăng nhập Admin", description = "Thay đổi Username hoặc Password của tài khoản Admin hiện tại đang đăng nhập.")
    public ResponseEntity<java.util.Map<String, String>> updateCredentials(
            @Valid @RequestBody com.project.anhgagifcode.application.port.in.dto.UpdateAdminCredentialsRequest request,
            java.security.Principal principal) {
        String currentUsername = principal.getName();
        updateAdminCredentialsUseCase.updateCredentials(currentUsername, request);
        return ResponseEntity.ok(java.util.Map.of("message", "Thay đổi thông tin đăng nhập thành công!"));
    }
}