package com.project.anhgagifcode.application.port.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu liên kết sản phẩm Kiotviet với trứng trong bể quà")
public class LinkProductToEggRequest {
    
    @NotNull(message = "ID sản phẩm Kiotviet không được để trống")
    @Schema(description = "ID của sản phẩm Kiotviet (kvProductId)")
    private Long productId;

    @NotBlank(message = "ID bể quà không được để trống")
    @Schema(description = "ID của bể quà (Gift Pool)")
    private String poolId;

    @NotNull(message = "Loại mapping không được để trống")
    @Min(value = 1, message = "Loại mapping không hợp lệ")
    @Max(value = 2, message = "Loại mapping không hợp lệ")
    @Schema(description = "Loại mapping (1: Trứng thường, 2: Trứng ấp)")
    private Integer mappingsType;
}
