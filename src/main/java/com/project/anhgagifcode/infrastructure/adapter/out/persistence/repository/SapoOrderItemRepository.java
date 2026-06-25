package com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository;

import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.SapoOrderItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SapoOrderItemRepository extends JpaRepository<SapoOrderItems, String> {
    
    // Hỗ trợ truy vấn danh sách item độc lập theo orderId nếu không muốn dùng JOIN
    List<SapoOrderItems> findByOrderId_Id(String orderId);
}