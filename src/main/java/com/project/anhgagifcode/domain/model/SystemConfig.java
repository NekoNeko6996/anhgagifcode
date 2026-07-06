package com.project.anhgagifcode.domain.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfig {
    private String configKey;
    private String configValue;
    private String description;
    private LocalDateTime updatedAt;
}
