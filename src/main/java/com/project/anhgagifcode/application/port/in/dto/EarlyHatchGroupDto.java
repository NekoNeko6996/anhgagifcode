package com.project.anhgagifcode.application.port.in.dto;

import lombok.Builder;
import lombok.Value;
import java.util.List;

@Value
@Builder
public class EarlyHatchGroupDto {
    private String customerCode;
    private int successCount;
    private int earlyHatchCredits;
    private List<OrderGroupDto> orders;
}
