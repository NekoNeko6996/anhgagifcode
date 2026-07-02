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
@Schema(description = "Thông tin khách hàng")
public class CustomerDto {
    @Schema(description = "ID khách hàng")
    private String id;
    
    @Schema(description = "Mã khách hàng KiotViet")
    private String customerCode;
    
    @Schema(description = "Tên khách hàng")
    private String customerName;
    
    @Schema(description = "Trạng thái (NEW, TRUSTED_1, TRUSTED_2, WARNING, BANNED)")
    private String status;
    
    @Schema(description = "Số lần mua hàng thành công")
    private int successCount;
    
    @Schema(description = "Chuỗi hoàn hàng hiện tại")
    private int returnStreak;
    
    @Schema(description = "Số lần cảnh báo")
    private int warningCount;
    
    @Schema(description = "Số lượt được duyệt sớm")
    private int earlyHatchCredits;

    @Schema(description = "Tổng số lượng đơn hoàn")
    private int returnCount;

    @Schema(description = "Thời gian tạo tài khoản")
    private LocalDateTime createdAt;
    
    @Schema(description = "Thời gian cập nhật gần nhất")
    private LocalDateTime updatedAt;
}
