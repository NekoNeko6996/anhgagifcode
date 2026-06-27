package com.project.anhgagifcode.application.port.out;

import com.project.anhgagifcode.domain.model.KiotvietOrder;
import com.project.anhgagifcode.domain.model.KiotvietProduct;
import java.util.List;
import java.util.Optional;

public interface KiotvietApiPort {
    // Gọi API của KiotViet để lấy dữ liệu hóa đơn mới nhất
    Optional<KiotvietOrder> fetchOrderFromKiotviet(String orderCode);
    List<KiotvietProduct> fetchAllProductsFromKiotviet();
}