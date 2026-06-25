package com.project.anhgagifcode.application.port.out;

import com.project.anhgagifcode.domain.model.Egg;
import java.util.List;
import java.util.Optional;

public interface EggPersistencePort {
    // Kèm khóa Pessimistic Lock chống Race Condition khi mở trứng
    Optional<Egg> loadEggByIdWithLock(String id);
    
    // Lấy danh sách trứng theo Order ID
    List<Egg> loadEggsByOrderId(String orderId);
    
    // Kiểm tra đơn hàng đã tạo loại trứng này chưa (chống tạo đúp)
    boolean existsByOrderIdAndEggType(String orderId, int eggType);
    
    // Lưu hoặc Cập nhật trứng
    Egg saveEgg(Egg egg);
    
    // Hủy toàn bộ trứng của một đơn hàng (dùng khi Webhook Sapo báo huỷ đơn)
    void cancelEggsByOrderId(String orderId);
}