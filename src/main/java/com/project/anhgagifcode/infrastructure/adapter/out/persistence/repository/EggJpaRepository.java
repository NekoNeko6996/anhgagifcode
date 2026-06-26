package com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository;

import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.Eggs;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EggJpaRepository extends JpaRepository<Eggs, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Eggs e WHERE e.id = :id")
    Optional<Eggs> findByIdForUpdate(@Param("id") String id);

    @Query("SELECT e FROM Eggs e WHERE e.orderId.id = :orderId")
    List<Eggs> findByOrderId(@Param("orderId") String orderId);

    @Modifying
    @Query("UPDATE Eggs e SET e.status = 'CANCELLED' WHERE e.orderId.id = :orderId")
    void cancelEggsByOrderId(@Param("orderId") String orderId);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN TRUE ELSE FALSE END FROM Eggs e WHERE e.orderId.id = :orderId AND e.giftPoolId.id = :poolId AND e.eggType = :eggType")
    boolean existsByOrderIdAndGiftPoolIdAndEggType(@Param("orderId") String orderId, @Param("poolId") String poolId, @Param("eggType") int eggType);
}