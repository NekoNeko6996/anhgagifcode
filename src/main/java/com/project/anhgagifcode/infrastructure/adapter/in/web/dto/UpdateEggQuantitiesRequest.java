package com.project.anhgagifcode.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu cấu hình số lượng trứng phát cho sản phẩm")
public class UpdateEggQuantitiesRequest {
    @Schema(description = "Số lượng trứng thường (Loại 1) phát cho mỗi sản phẩm", example = "1")
    @Min(value = 0, message = "Số lượng trứng Loại 1 không được nhỏ hơn 0")
    private int eggType1Qty;

    @Schema(description = "Số lượng trứng ấp (Loại 2) phát cho mỗi sản phẩm", example = "1")
    @Min(value = 0, message = "Số lượng trứng Loại 2 không được nhỏ hơn 0")
    private int eggType2Qty;
}
