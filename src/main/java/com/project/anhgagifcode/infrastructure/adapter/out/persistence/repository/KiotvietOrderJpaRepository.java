package com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository;

import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.KiotvietOrders;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface KiotvietOrderJpaRepository extends JpaRepository<KiotvietOrders, String> {
    @org.springframework.data.jpa.repository.Query("SELECT k FROM KiotvietOrders k LEFT JOIN FETCH k.kiotvietOrderItemsCollection WHERE k.orderCode = :orderCode")
    Optional<KiotvietOrders> findByOrderCode(@org.springframework.data.repository.query.Param("orderCode") String orderCode);

    @org.springframework.data.jpa.repository.Query("SELECT k FROM KiotvietOrders k LEFT JOIN FETCH k.kiotvietOrderItemsCollection WHERE k.orderCode IN :orderCodes")
    java.util.List<KiotvietOrders> findByOrderCodeIn(@org.springframework.data.repository.query.Param("orderCodes") java.util.Collection<String> orderCodes);

    @org.springframework.data.jpa.repository.Query(value = "SELECT DISTINCT SUBSTRING_INDEX(order_code, '_', 1) FROM kiotviet_orders WHERE order_code LIKE '%\\_%'", nativeQuery = true)
    java.util.List<String> findDistinctPrefixes();

    @org.springframework.data.jpa.repository.Query("SELECT k FROM KiotvietOrders k LEFT JOIN FETCH k.kiotvietOrderItemsCollection WHERE k.customerCode.customerCode = :customerCode")
    java.util.List<KiotvietOrders> findByCustomerCode(@org.springframework.data.repository.query.Param("customerCode") String customerCode);
}