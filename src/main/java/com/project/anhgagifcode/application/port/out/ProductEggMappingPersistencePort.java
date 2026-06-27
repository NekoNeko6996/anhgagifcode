package com.project.anhgagifcode.application.port.out;

import com.project.anhgagifcode.domain.model.ProductEggMapping;
import java.util.List;

public interface ProductEggMappingPersistencePort {
    
    // Truyền vào danh sách ID sản phẩm khách đã mua, trả về danh sách các luật tương ứng
    List<ProductEggMapping> loadMappingsByProductIds(List<String> kvProductIds);

    List<ProductEggMapping> findAll();
}