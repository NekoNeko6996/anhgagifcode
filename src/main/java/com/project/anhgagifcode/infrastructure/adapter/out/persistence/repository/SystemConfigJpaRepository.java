package com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository;

import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.SystemConfigs;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemConfigJpaRepository extends JpaRepository<SystemConfigs, String> {
}
