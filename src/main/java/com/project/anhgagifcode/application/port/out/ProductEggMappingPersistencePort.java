package com.project.anhgagifcode.application.port.out;

import com.project.anhgagifcode.domain.model.ProductEggMapping;
import java.util.List;
import java.util.Optional;

public interface ProductEggMappingPersistencePort {
    
    // Truyền vào danh sách ID sản phẩm khách đã mua, trả về danh sách các luật tương ứng
    List<ProductEggMapping> loadMappingsByProductIds(List<String> kvProductIds);

    List<ProductEggMapping> findAll();

    List<ProductEggMapping> findByKvProductId(Long kvProductId);

    boolean existsByKvProductIdAndEggType(Long kvProductId, int eggType);

    void saveMapping(Long kvProductId, String poolId, int eggType);

    void deleteMapping(String mappingId);

    void deleteMappings(List<String> mappingIds);
}