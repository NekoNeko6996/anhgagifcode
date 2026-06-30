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

    @Query(value = "SELECT g.* FROM gift_accounts g JOIN pool_account_mappings p ON g.id = p.account_id WHERE p.pool_id = :poolId AND g.status = 'AVAILABLE' LIMIT 1 FOR UPDATE", nativeQuery = true)
    java.util.Optional<GiftAccounts> findAvailableAccountForUpdateSkipLocked(@Param("poolId") String poolId);

    @Query("SELECT g FROM GiftAccounts g JOIN PoolAccountMappings p ON g.id = p.accountId.id WHERE p.poolId.id = :poolId")
    java.util.List<GiftAccounts> findAccountsByPoolId(@Param("poolId") String poolId);

    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM GiftAccounts g WHERE g.id IN :accountIds")
    void deleteByIdIn(@Param("accountIds") java.util.List<String> accountIds);
}