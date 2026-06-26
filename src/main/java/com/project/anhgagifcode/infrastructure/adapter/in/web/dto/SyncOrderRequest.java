package com.project.anhgagifcode.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Payload yêu cầu tra cứu và đồng bộ đơn hàng")
public class SyncOrderRequest {

    @NotBlank(message = "Mã đơn hàng không được để trống")
    @Size(min = 5, max = 50, message = "Mã đơn hàng phải từ 5 đến 50 ký tự")
    // Regex siêu chặt: Chỉ cho phép chữ cái, số, gạch ngang và gạch dưới (Chống SQLi, XSS)
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Mã đơn hàng chứa ký tự không hợp lệ")
    @Schema(description = "Mã đơn hàng từ KiotViet do khách hàng nhập", example = "DHSPE_260622PSYR4BHX")
    private String orderCode;
}