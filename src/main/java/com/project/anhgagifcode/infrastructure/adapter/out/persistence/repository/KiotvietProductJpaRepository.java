package com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository;

import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.KiotvietProducts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository                                                     
public interface KiotvietProductJpaRepository extends JpaRepository<KiotvietProducts, Long> {
}