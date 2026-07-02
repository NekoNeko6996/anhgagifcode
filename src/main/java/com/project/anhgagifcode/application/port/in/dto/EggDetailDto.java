package com.project.anhgagifcode.application.port.in.dto;

import lombok.Builder;
import lombok.Value;
import java.time.LocalDateTime;

@Value
@Builder
public class EggDetailDto {
    private String eggId;
    private int eggType;
    private LocalDateTime hatchAt;
}
