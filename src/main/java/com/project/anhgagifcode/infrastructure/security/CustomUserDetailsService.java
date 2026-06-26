package com.project.anhgagifcode.infrastructure.security;

import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.Admins;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper.AdminMapper;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository.AdminJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AdminJpaRepository adminJpaRepository;
    private final AdminMapper adminMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admins entity = adminJpaRepository.findByUsernameAndStatus(username, "ACTIVE")
                .orElseThrow(() -> new UsernameNotFoundException("Tài khoản không tồn tại hoặc bị khóa"));
        return new CustomUserDetails(adminMapper.toDomain(entity));
    }
}