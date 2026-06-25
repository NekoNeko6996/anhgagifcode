package com.project.anhgagifcode.domain.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GiftPool {
    private String id;
    private String poolName;
    private String tier;
    private LocalDateTime createdAt;
}