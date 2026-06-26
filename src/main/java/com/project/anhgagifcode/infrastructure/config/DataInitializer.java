package com.project.anhgagifcode.infrastructure.config;

import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.Admins;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository.AdminJpaRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminJpaRepository adminJpaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Kiểm tra xem bảng Admins đã có dữ liệu chưa
        if (adminJpaRepository.count() == 0) {
            log.info("Không tìm thấy tài khoản Admin nào. Đang tiến hành tạo tài khoản mặc định...");

            Admins defaultAdmin = new Admins();
            defaultAdmin.setId(UUID.randomUUID().toString());
            defaultAdmin.setUsername("admin");
            defaultAdmin.setFullName("admin");
            defaultAdmin.setPasswordHash(passwordEncoder.encode("admin123")); 
            defaultAdmin.setRole("SUPER_ADMIN");
            defaultAdmin.setStatus("ACTIVE");
            defaultAdmin.setCreatedAt(Date.from(Instant.now()));

            adminJpaRepository.save(defaultAdmin);
            
            log.info("Khởi tạo thành công! Username: admin | Password: admin123");
        }
    }
}