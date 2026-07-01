package com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository;

import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.ProductEggMappings;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductEggMappingJpaRepository extends JpaRepository<ProductEggMappings, String> {
    @org.springframework.data.jpa.repository.Query("SELECT p FROM ProductEggMappings p WHERE p.kvProductId.kvProductId IN :kvProductIds")
    List<ProductEggMappings> findByKvProductIdIn(@org.springframework.data.repository.query.Param("kvProductIds") List<Long> kvProductIds);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM ProductEggMappings p WHERE p.kvProductId.kvProductId = :kvProductId")
    List<ProductEggMappings> findByProductId(@org.springframework.data.repository.query.Param("kvProductId") Long kvProductId);
}