package com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository;

import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.Admins;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AdminJpaRepository extends JpaRepository<Admins, String> {
    Optional<Admins> findByUsernameAndStatus(String username, String status);
}