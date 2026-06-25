package com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository;

import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.GiftAccounts;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GiftAccountRepository extends JpaRepository<GiftAccounts, String> {

    // Đếm tổng số lượng tài khoản KHẢ DỤNG trong 1 Pool (dùng để tính offset random)
    @Query("SELECT COUNT(g) FROM GiftAccounts g JOIN g.poolAccountMappingsCollection p WHERE g.status = 'AVAILABLE' AND p.poolId.id = :poolId")
    long countAvailableByPoolId(@Param("poolId") String poolId);

    // Lấy 1 tài khoản khả dụng kèm theo Khóa PESSIMISTIC_WRITE
    // Tham số Pageable dùng để truyền "LIMIT randomOffset, 1" từ tầng Service
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM GiftAccounts g JOIN g.poolAccountMappingsCollection p WHERE g.status = 'AVAILABLE' AND p.poolId.id = :poolId")
    List<GiftAccounts> findAvailableByPoolIdWithLock(@Param("poolId") String poolId, Pageable pageable);
}