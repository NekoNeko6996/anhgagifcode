package com.project.anhgagifcode.application.port.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin tài khoản quà tặng")
public class GiftAccountDto {
    @Schema(description = "ID tài khoản quà")
    private String id;
    
    @Schema(description = "Tài khoản đăng nhập")
    private String username;
    
    @Schema(description = "Mật khẩu")
    private String password;
    
    @Schema(description = "Trạng thái (AVAILABLE, ASSIGNED)")
    private String status;
    
    @Schema(description = "Phân cấp (Tier)")
    private String tier;
    
    @Schema(description = "Nền tảng (Platform)")
    private String platform;
    
    @Schema(description = "Token quà tặng")
    private String token;
    
    @Schema(description = "Thời gian tạo")
    private LocalDateTime createdAt;
    
    @Schema(description = "Thời gian gán cho khách hàng")
    private LocalDateTime assignedAt;
}
