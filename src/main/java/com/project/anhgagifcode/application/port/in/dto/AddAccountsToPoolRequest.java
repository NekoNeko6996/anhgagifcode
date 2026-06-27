package com.project.anhgagifcode.application.port.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu thêm nhiều tài khoản vào bể quà một lúc")
public class AddAccountsToPoolRequest {
    
    @NotBlank(message = "ID bể quà không được để trống")
    @Schema(description = "ID của bể quà (Gift Pool)")
    private String poolId;

    @NotEmpty(message = "Danh sách ID tài khoản không được để trống")
    @Schema(description = "Danh sách ID của các tài khoản quà tặng (Gift Accounts)")
    private List<String> accountIds;
}
