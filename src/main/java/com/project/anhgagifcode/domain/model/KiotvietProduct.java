package com.project.anhgagifcode.domain.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KiotvietProduct {         
    private Long kvProductId;     
    private String code;
    private String name;        
    private String fullName;      
    private Double basePrice;     
    private String imageUrl;      
    private LocalDateTime lastSyncedAt; 
}