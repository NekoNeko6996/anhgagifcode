package com.project.anhgagifcode.domain.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EggOpeningLog {
    private String id;
    private String actionType;
    private String triggeredBy;
    private String ipAddress;
    private LocalDateTime createdAt;
    private String accountId;
    private String eggId;
}