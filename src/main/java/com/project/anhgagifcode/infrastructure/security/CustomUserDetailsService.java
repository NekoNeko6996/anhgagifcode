package com.project.anhgagifcode.infrastructure.security;

import com.project.anhgagifcode.application.port.out.AdminPersistencePort;
import com.project.anhgagifcode.domain.model.Admin;
import com.project.anhgagifcode.infrastructure.security.model.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AdminPersistencePort adminPersistencePort;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = adminPersistencePort.loadActiveAdminByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy admin hoặc tài khoản bị khóa: " + username));
        return new CustomUserDetails(admin);
    }
}