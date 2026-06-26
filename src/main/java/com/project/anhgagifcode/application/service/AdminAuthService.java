package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.AdminAuthUseCase;
import com.project.anhgagifcode.domain.exception.BusinessRuleViolationException;
import com.project.anhgagifcode.infrastructure.adapter.in.web.dto.AdminLoginRequest;
import com.project.anhgagifcode.infrastructure.adapter.in.web.dto.AdminLoginResponse;
import com.project.anhgagifcode.infrastructure.security.CustomUserDetails;
import com.project.anhgagifcode.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthService implements AdminAuthUseCase {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Override
    public AdminLoginResponse login(AdminLoginRequest request) {
        try {
            // 1. Xác thực tài khoản và mật khẩu thông qua Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // 2. Lưu thông tin vào Security Context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3. Lấy thông tin user đã được xác thực
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // 4. Tạo JWT Token
            String jwt = tokenProvider.generateToken(userDetails.getUsername());

            // 5. Trả về kết quả
            return AdminLoginResponse.builder()
                    .accessToken(jwt)
                    .tokenType("Bearer")
                    .username(userDetails.getUsername())
                    .role(userDetails.getAdmin().getRole())
                    .build();

        } catch (BadCredentialsException ex) {
            log.warn("Đăng nhập thất bại: Sai tài khoản hoặc mật khẩu - Username: {}", request.getUsername());
            throw new BusinessRuleViolationException("Tài khoản hoặc mật khẩu không chính xác!");
        } catch (AuthenticationException ex) {
            log.error("Lỗi hệ thống khi đăng nhập: ", ex);
            throw new BusinessRuleViolationException("Đăng nhập thất bại do lỗi hệ thống.");
        }
    }
}