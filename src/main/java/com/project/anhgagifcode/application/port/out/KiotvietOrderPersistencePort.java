package com.project.anhgagifcode.application.port.out;

import com.project.anhgagifcode.domain.model.KiotvietOrder;
import java.util.List;
import java.util.Optional;

public interface KiotvietOrderPersistencePort {

    // Tìm đơn hàng bằng mã đơn để kiểm tra xem đã đồng bộ chưa
    Optional<KiotvietOrder> loadByOrderCode(String orderCode);

    // Lưu đơn hàng (Cần lưu cả KiotvietOrder và danh sách KiotvietOrderItems bên trong)
    KiotvietOrder saveOrder(KiotvietOrder order);

    List<KiotvietOrder> findAll();

    List<KiotvietOrder> findByCustomerCode(String customerCode);
}
