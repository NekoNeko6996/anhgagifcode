package com.project.anhgagifcode.domain.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SapoOrderItem {
    private String id;
    private String sapoProductId;
    private String sapoVariantId;
    private String sku;
    private int quantity;
    private String orderId; // Chỉ lưu ID của Order cha để tránh vòng lặp tham chiếu
}