package com.project.anhgagifcode.application.port.in.dto;

import lombok.Builder;
import lombok.Value;
import java.util.List;

@Value
@Builder
public class OrderGroupDto {
    private String orderId;
    private String orderCode;
    private String skuDetails;
    private List<EggDetailDto> eggs;
}
