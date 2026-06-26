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
    private int eggType; // Dùng 1 hoặc 2 để phân biệt logic ấp
    private String status;
    private LocalDateTime hatchAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Đổi tham chiếu từ SapoOrder sang KiotvietOrder
    private KiotvietOrder order;
    private GiftAccount account;
    private GiftPool giftPool;
}