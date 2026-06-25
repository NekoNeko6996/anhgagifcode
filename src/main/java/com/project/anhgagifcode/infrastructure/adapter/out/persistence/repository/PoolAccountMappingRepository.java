package com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository;

import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.PoolAccountMappings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PoolAccountMappingRepository extends JpaRepository<PoolAccountMappings, String> {

    // Xóa liên kết tài khoản khỏi kho khi Admin thao tác xóa mềm Account
    @Modifying
    @Query("DELETE FROM PoolAccountMappings p WHERE p.accountId.id = :accountId")
    void deleteByAccountId(@Param("accountId") String accountId);
}