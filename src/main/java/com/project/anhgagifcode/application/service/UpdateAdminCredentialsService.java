package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.UpdateAdminCredentialsUseCase;
import com.project.anhgagifcode.application.port.in.dto.UpdateAdminCredentialsRequest;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.Admins;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository.AdminJpaRepository;
import com.project.anhgagifcode.domain.exception.BusinessRuleViolationException;
import com.project.anhgagifcode.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
public class UpdateAdminCredentialsService implements UpdateAdminCredentialsUseCase {

    private final AdminJpaRepository adminJpaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void updateCredentials(String currentUsername, UpdateAdminCredentialsRequest request) {
        Admins admin = adminJpaRepository.findByUsernameAndStatus(currentUsername, "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản quản trị viên không khả dụng."));

        // 1. Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), admin.getPasswordHash())) {
            throw new BusinessRuleViolationException("Mật khẩu hiện tại không chính xác.");
        }

        // 2. Update username if provided and different
        if (request.getNewUsername() != null && !request.getNewUsername().isBlank()) {
            String newUsername = request.getNewUsername().trim();
            if (!newUsername.equalsIgnoreCase(currentUsername)) {
                // Check uniqueness
                Optional<Admins> existing = adminJpaRepository.findByUsernameAndStatus(newUsername, "ACTIVE");
                if (existing.isPresent()) {
                    throw new BusinessRuleViolationException("Tên đăng nhập mới đã tồn tại trên hệ thống.");
                }
                admin.setUsername(newUsername);
            }
        }

        // 3. Update password if provided
        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            admin.setPasswordHash(passwordEncoder.encode(request.getNewPassword().trim()));
        }

        admin.setUpdatedAt(new java.util.Date());
        adminJpaRepository.save(admin);
    }
}
