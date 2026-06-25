package com.project.anhgagifcode.domain.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductEggMapping {
    private String id;
    private String sapoProductId;
    private String sapoVariantId;
    private int eggType;
    private String eggTier;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private GiftPool giftPool;
}