package com.project.anhgagifcode.domain.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KiotvietOrder {
    private String id;
    private String orderCode;
    private String customerCode;
    private String deliveryStatus;
    private LocalDateTime lastSyncedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Aggregate Root pattern
    private List<KiotvietOrderItem> orderItems;
}