package com.project.anhgagifcode.domain.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {
    private String id;
    private String customerCode;
    private String customerName;
    private String status;
    private int successCount;
    private int returnStreak;
    private int warningCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}