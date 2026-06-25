package com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository;

import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.ProductEggMappings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductEggMappingRepository extends JpaRepository<ProductEggMappings, String> {

    // Trả về luật Mapping có Tier cao nhất cho một tập hợp các Sản phẩm trong đơn hàng
    // Spring Data JPA sẽ tự động tạo câu SQL: ORDER BY egg_tier DESC LIMIT 1
    Optional<ProductEggMappings> findFirstBySapoProductIdInOrderByEggTierDesc(List<String> sapoProductIds);

    // Kiểm tra trùng lặp luật khi Admin thao tác
    boolean existsBySapoProductIdAndEggType(String sapoProductId, int eggType);
}