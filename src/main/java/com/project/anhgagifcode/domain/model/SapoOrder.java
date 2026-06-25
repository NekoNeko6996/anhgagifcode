package com.project.anhgagifcode.domain.model;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SapoOrder {
    private String id;
    private String orderCode;
    private String sourceName;
    private BigDecimal totalPrice;
    private String financialStatus;
    private String fulfillmentStatus;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Order chứa list OrderItem (Aggregate Root pattern)
    private List<SapoOrderItem> orderItems;
}