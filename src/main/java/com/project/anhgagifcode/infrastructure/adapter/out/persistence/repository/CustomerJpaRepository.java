package com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository;

import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.Customers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface CustomerJpaRepository extends JpaRepository<Customers, String> {
    Optional<Customers> findByCustomerCode(String customerCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Customers c WHERE c.customerCode = :customerCode")
    Optional<Customers> findByCustomerCodeForUpdate(@Param("customerCode") String customerCode);
}