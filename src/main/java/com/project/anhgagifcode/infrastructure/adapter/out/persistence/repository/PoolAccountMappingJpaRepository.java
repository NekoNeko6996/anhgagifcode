package com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository;

import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.PoolAccountMappings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PoolAccountMappingJpaRepository extends JpaRepository<PoolAccountMappings, String> {
    boolean existsByPoolIdIdAndAccountIdId(String poolId, String accountId);

    @Query("SELECT p.accountId.id FROM PoolAccountMappings p WHERE p.poolId.id = :poolId AND p.accountId.id IN :accountIds")
    List<String> findExistingAccountIdsInPool(@Param("poolId") String poolId, @Param("accountIds") List<String> accountIds);

    @Modifying
    @Query("DELETE FROM PoolAccountMappings p WHERE p.poolId.id = :poolId AND p.accountId.id IN :accountIds")
    void deleteByPoolIdAndAccountIds(@Param("poolId") String poolId, @Param("accountIds") List<String> accountIds);

    @Modifying
    @Query("DELETE FROM PoolAccountMappings p WHERE p.accountId.id IN :accountIds")
    void deleteByAccountIdIn(@Param("accountIds") List<String> accountIds);
}
