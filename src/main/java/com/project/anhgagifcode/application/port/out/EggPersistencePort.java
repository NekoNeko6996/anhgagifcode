package com.project.anhgagifcode.application.port.out;

import com.project.anhgagifcode.domain.model.Egg;
import java.util.List;
import java.util.Optional;

public interface EggPersistencePort {
    
    // Lưu thông tin quả trứng (khi mới sinh ra hoặc khi update status)
    Egg saveEgg(Egg egg);
    
    // Load Trứng bằng ID (Dùng khi khách bấm "Mở Trứng")
    // Lưu ý: Cần hỗ trợ Pessimistic Lock ở tầng Adapter để chống Race Condition
    Optional<Egg> loadEggForUpdate(String eggId);
    
    // Lấy toàn bộ trứng của một đơn hàng (Dùng để hiển thị cho UI)
    List<Egg> loadEggsByOrderId(String orderId);
    
    // Cập nhật hàng loạt: Khi đơn hàng bị hoàn, chuyển tất cả trứng của đơn đó sang CANCELLED
    void cancelEggsByOrderId(String orderId);
    
    // Kiểm tra xem đơn hàng này đã từng sinh trứng thuộc Pool này chưa (Tránh duplicate)
    boolean existsByOrderIdAndPoolIdAndEggType(String orderId, String poolId, int eggType);
}