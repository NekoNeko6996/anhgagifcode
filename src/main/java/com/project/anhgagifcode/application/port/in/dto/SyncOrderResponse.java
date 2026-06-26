package com.project.anhgagifcode.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import lombok.Value;

@Getter
@Builder
@Value
public class SyncOrderResponse {
    private String customerName;
    private String customerStatus; // Hiển thị cấp độ VIP của khách (TRUSTED_1, TRUSTED_2)
    private String deliveryStatus;
    private List<EggDisplayDto> eggs;
}