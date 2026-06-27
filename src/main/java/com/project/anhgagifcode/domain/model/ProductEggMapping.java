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
    private KiotvietProduct productCode;  
    private int eggType;          
    private GiftPool giftPoolId;   
    private String eggTier;       
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}