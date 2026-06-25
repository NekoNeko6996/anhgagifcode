package com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository;

import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.SapoOrders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SapoOrderRepository extends JpaRepository<SapoOrders, String> {

    // Tìm đơn hàng theo mã (được index UNIQUE ở database)
    Optional<SapoOrders> findByOrderCode(String orderCode);
}