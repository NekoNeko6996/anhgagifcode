package com.project.anhgagifcode.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Payload đăng nhập của Admin")
public class AdminLoginRequest {

    @NotBlank(message = "Tài khoản không được để trống")
    @Schema(example = "admin")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Schema(example = "admin123")
    private String password;
}