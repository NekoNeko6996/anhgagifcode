package com.project.anhgagifcode.application.port.out;

import com.project.anhgagifcode.domain.model.ProductEggMapping;
import java.util.List;
import java.util.Optional;

public interface ProductEggMappingPersistencePort {
    // Dựa vào danh sách Product ID của đơn hàng, lấy ra Luật (Mapping) có Tier cao nhất
    Optional<ProductEggMapping> loadHighestTierMapping(List<String> sapoProductIds);
    
    // Kiểm tra xem luật quy đổi cho sản phẩm này đã tồn tại chưa
    boolean existsMapping(String sapoProductId, int eggType);
    
    ProductEggMapping saveMapping(ProductEggMapping mapping);
    void deleteMapping(String id);
}