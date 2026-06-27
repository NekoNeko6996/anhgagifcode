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
@Schema(description = "Yêu cầu thay đổi thông tin đăng nhập admin")
public class UpdateAdminCredentialsRequest {

    @NotBlank(message = "Mật khẩu cũ không được để trống")
    @Schema(description = "Mật khẩu hiện tại để xác minh danh tính")
    private String oldPassword;

    @Schema(description = "Tên đăng nhập mới (để trống nếu không đổi)")
    private String newUsername;

    @Schema(description = "Mật khẩu mới (để trống nếu không đổi)")
    private String newPassword;
}
