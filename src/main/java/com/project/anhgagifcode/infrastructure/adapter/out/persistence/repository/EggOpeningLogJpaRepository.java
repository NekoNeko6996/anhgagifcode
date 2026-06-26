package com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository;

import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.EggOpeningLogs;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EggOpeningLogJpaRepository extends JpaRepository<EggOpeningLogs, String> {
}