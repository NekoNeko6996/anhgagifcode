package com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository;

import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.Admins;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admins, String> {

    // Chỉ lấy tài khoản Admin đang hoạt động
    @Query("SELECT a FROM Admins a WHERE a.username = :username AND a.status = 'ACTIVE'")
    Optional<Admins> findActiveByUsername(@Param("username") String username);
}