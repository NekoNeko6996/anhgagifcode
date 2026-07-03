package com.project.anhgagifcode.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "Payload yêu cầu mở trứng nhận quà")
public class ClaimEggRequest {

    @NotBlank(message = "ID Đơn hàng không được để trống")
    @Schema(description = "Mã định danh của đơn hàng", example = "550e8400-e29b-41d4-a716-446655440000")
    private String orderId;

    @Schema(description = "Loại trứng (1: Thường, 2: Cần ấp)", example = "1")
    private int eggType;
}