package com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository;

import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.EggOpeningLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EggOpeningLogJpaRepository extends JpaRepository<EggOpeningLogs, String> {

    @Modifying
    @Query("DELETE FROM EggOpeningLogs l WHERE l.accountId.id IN :accountIds")
    void deleteByAccountIdIn(@Param("accountIds") List<String> accountIds);
}