package com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository;

import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.Eggs;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.SapoOrders;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EggRepository extends JpaRepository<Eggs, String> {

    // Lấy trứng và JOIN luôn SapoOrder trong 1 query.
    // Áp dụng Pessimistic Lock để khóa record, chặn các luồng khác can thiệp khi đang Claim.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Eggs e JOIN FETCH e.orderId WHERE e.id = :id")
    Optional<Eggs> findByIdWithLock(@Param("id") String id);

    // Tìm toàn bộ trứng của một đơn hàng
    List<Eggs> findByOrderId(SapoOrders orderId);
    
    // Kiểm tra xem đơn hàng này đã sinh loại trứng này chưa
    boolean existsByOrderIdAndEggType(SapoOrders orderId, int eggType);
}