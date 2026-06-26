package com.project.anhgagifcode.domain.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KiotvietOrderItem {
    private String id;
    private String kvProductId;
    private int quantity;
    private LocalDateTime lastSyncedAt;
    private String orderId; // Chỉ lưu ID của Order cha
}