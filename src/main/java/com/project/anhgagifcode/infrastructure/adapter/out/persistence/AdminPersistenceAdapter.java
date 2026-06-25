package com.project.anhgagifcode.infrastructure.adapter.out.persistence;

import com.project.anhgagifcode.application.port.out.AdminPersistencePort;
import com.project.anhgagifcode.domain.model.Admin;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper.AdminMapper;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AdminPersistenceAdapter implements AdminPersistencePort {

    private final AdminRepository adminRepository;
    private final AdminMapper adminMapper;

    @Override
    public Optional<Admin> loadActiveAdminByUsername(String username) {
        return adminRepository.findActiveByUsername(username).map(adminMapper::toDomain);
    }
}