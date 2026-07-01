package com.project.anhgagifcode.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMappingRateRequest {
    @NotNull(message = "ID liên kết không được để trống")
    private String mappingId;

    @NotNull(message = "Tỉ lệ phần trăm không được để trống")
    private Double rate;
}
