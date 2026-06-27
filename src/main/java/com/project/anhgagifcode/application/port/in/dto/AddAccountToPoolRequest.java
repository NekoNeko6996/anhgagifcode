package com.project.anhgagifcode.application.port.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu thêm tài khoản vào bể quà")
public class AddAccountToPoolRequest {
    
    @NotBlank(message = "ID bể quà không được để trống")
    @Schema(description = "ID của bể quà (Gift Pool)")
    private String poolId;

    @NotBlank(message = "ID tài khoản không được để trống")
    @Schema(description = "ID của tài khoản quà tặng (Gift Account)")
    private String accountId;
}
