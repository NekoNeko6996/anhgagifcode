package com.project.anhgagifcode.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import lombok.Value;

@Getter
@Builder
@Value
public class EggDisplayDto {
    private String eggId;
    private int eggType; // 1: Thường, 2: Cần ấp
    private String displayStatus; // READY_TO_CLAIM, HATCHING, WAITING_ORDER_COMPLETION, CANCELLED
    private LocalDateTime hatchAt; // Thời gian đếm ngược (nếu có)
}