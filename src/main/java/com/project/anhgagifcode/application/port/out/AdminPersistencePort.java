package com.project.anhgagifcode.application.port.out;

import com.project.anhgagifcode.domain.model.Admin;
import java.util.Optional;

public interface AdminPersistencePort {
    // Chỉ lấy Admin có trạng thái ACTIVE để cho phép Login
    Optional<Admin> loadActiveAdminByUsername(String username);
}