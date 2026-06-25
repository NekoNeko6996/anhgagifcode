package com.project.anhgagifcode.application.port.out;

public interface SapoApiPort {
    // Gọi API sang Sapo lấy JSON chi tiết của 1 Order
    // (Có thể trả về String JSON thuần, hoặc một DTO trung gian, tùy bạn thiết kế sau)
    String fetchOrderFromSapo(String orderCode);
    
    // Gọi API sang Sapo lấy danh sách Products
    String fetchAllProductsFromSapo();
}