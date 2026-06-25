package com.project.anhgagifcode.application.port.out;

import com.project.anhgagifcode.domain.model.SapoOrder;
import java.util.Optional;

public interface SapoOrderPersistencePort {
    // Tìm đơn hàng bằng mã order_code (VD: SON123456)
    Optional<SapoOrder> loadOrderByCode(String orderCode);
    
    // UPSERT (Thêm mới hoặc Cập nhật) đơn hàng kèm theo Order Items
    SapoOrder saveOrder(SapoOrder order);
}