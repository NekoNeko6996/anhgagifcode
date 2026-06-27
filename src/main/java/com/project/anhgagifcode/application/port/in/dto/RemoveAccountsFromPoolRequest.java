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
@Schema(description = "Yêu cầu xóa liên kết tài khoản khỏi bể quà")
public class RemoveAccountsFromPoolRequest {
    
    @NotBlank(message = "ID bể quà không được để trống")
    @Schema(description = "ID của bể quà (Gift Pool)")
    private String poolId;

    @NotEmpty(message = "Danh sách ID tài khoản không được để trống")
    @Schema(description = "Danh sách ID tài khoản cần xóa khỏi bể quà")
    private List<String> accountIds;
}
