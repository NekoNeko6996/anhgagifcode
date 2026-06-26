package com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository;

import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.ProductEggMappings;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductEggMappingJpaRepository extends JpaRepository<ProductEggMappings, String> {
    List<ProductEggMappings> findByKvProductIdIn(List<String> kvProductIds);
}