package com.project.anhgagifcode.application.port.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu tạo bể quà mới")
public class CreateGiftPoolRequest {
    
    @NotBlank(message = "Tên bể quà không được để trống")
    @Size(max = 150, message = "Tên bể quà không quá 150 ký tự")
    @Schema(description = "Tên bể quà")
    private String poolName;

    @NotBlank(message = "Tier của bể quà không được để trống")
    @Pattern(regexp = "^[A-Z]$", message = "Tier phải là một ký tự viết hoa từ A-Z (A, B, C, D...)")
    @Schema(description = "Tier của bể quà (A, B, C, D...)")
    private String tier;
}
