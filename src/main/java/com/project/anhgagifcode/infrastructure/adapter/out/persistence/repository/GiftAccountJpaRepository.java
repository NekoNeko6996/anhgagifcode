package com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository;

import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.GiftAccounts;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GiftAccountJpaRepository extends JpaRepository<GiftAccounts, String> {

    @Query("SELECT COUNT(g) FROM GiftAccounts g JOIN PoolAccountMappings p ON g.id = p.accountId.id WHERE p.poolId.id = :poolId AND g.status = 'AVAILABLE'")
    long countAvailableByPoolId(@Param("poolId") String poolId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM GiftAccounts g JOIN PoolAccountMappings p ON g.id = p.accountId.id WHERE p.poolId.id = :poolId AND g.status = 'AVAILABLE'")
    Page<GiftAccounts> findAvailableAccountForUpdate(@Param("poolId") String poolId, Pageable pageable);
}