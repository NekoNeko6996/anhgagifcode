package com.project.anhgagifcode.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Payload thêm mới 1 tài khoản nhận quà")
public class CreateGiftAccountRequest {

    @NotBlank(message = "Tài khoản không được để trống")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    @NotBlank(message = "Tier (Cấp độ) không được để trống")
    private String tier;

    private String token;
    private String platform;
}