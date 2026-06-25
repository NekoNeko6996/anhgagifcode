package com.project.anhgagifcode.domain.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Egg {
    private String id;
    private int eggType;
    private String status;
    private LocalDateTime hatchAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Domain Model chứa Object reference đến các entity quan trọng để dễ truy xuất
    private SapoOrder order;
    private GiftAccount account;
    private GiftPool giftPool;
}