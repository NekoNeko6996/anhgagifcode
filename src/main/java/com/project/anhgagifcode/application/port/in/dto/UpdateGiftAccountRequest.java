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
@Schema(description = "Yêu cầu cập nhật thông tin tài khoản quà tặng")
public class UpdateGiftAccountRequest {

    @NotBlank(message = "Tài khoản không được để trống")
    @Schema(description = "Tên đăng nhập tài khoản")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Schema(description = "Mật khẩu tài khoản")
    private String password;

    @NotBlank(message = "Nền tảng không được để trống")
    @Schema(description = "Nền tảng (Roblox, Steam, v.v.)")
    private String platform;

    @NotBlank(message = "Trạng thái không được để trống")
    @Schema(description = "Trạng thái tài khoản (AVAILABLE, ASSIGNED, v.v.)")
    private String status;

    @Schema(description = "Token xác thực của tài khoản (nếu có)")
    private String token;
}
