package com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository;

import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.KiotvietOrders;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface KiotvietOrderJpaRepository extends JpaRepository<KiotvietOrders, String> {
    Optional<KiotvietOrders> findByOrderCode(String orderCode);

    @org.springframework.data.jpa.repository.Query("SELECT k FROM KiotvietOrders k WHERE k.customerCode.customerCode = :customerCode")
    java.util.List<KiotvietOrders> findByCustomerCode(@org.springframework.data.repository.query.Param("customerCode") String customerCode);
}