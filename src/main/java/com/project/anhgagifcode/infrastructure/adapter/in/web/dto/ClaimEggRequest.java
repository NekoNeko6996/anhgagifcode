package com.project.anhgagifcode.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "Payload yêu cầu mở trứng nhận quà")
public class ClaimEggRequest {

    @NotBlank(message = "ID Trứng không được để trống")
    // Kiểm tra định dạng chuẩn UUID (36 ký tự) để chống truyền payload bẩn
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$", 
            message = "ID Trứng không hợp lệ")
    @Schema(description = "Mã định danh (UUID) của quả trứng muốn mở", example = "550e8400-e29b-41d4-a716-446655440000")
    private String eggId;
}