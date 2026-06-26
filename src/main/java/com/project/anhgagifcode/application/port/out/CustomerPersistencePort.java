package com.project.anhgagifcode.application.port.out;

import com.project.anhgagifcode.domain.model.Customer;
import java.util.Optional;

public interface CustomerPersistencePort {
    
    // Tìm khách hàng qua mã KiotViet
    Optional<Customer> loadByCustomerCode(String customerCode);
    
    // Lưu hoặc Cập nhật thông tin khách hàng (bao gồm logic tăng success_count, return_streak...)
    Customer saveCustomer(Customer customer);
}