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
    private String customerStatus;
    private String orderId;
    private String deliveryStatus;
    private List<EggDisplayDto> eggs;
    private int totalType1Eggs;
    private int totalType2Eggs;
}