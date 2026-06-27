package com.project.anhgagifcode.application.port.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin đơn hàng KiotViet kèm danh sách mặt hàng")
public class KiotvietOrderDto {
    @Schema(description = "ID đơn hàng")
    private String id;
    
    @Schema(description = "Mã đơn hàng KiotViet")
    private String orderCode;
    
    @Schema(description = "Mã khách hàng")
    private String customerCode;
    
    @Schema(description = "Trạng thái giao hàng")
    private String deliveryStatus;
    
    @Schema(description = "Thời gian đồng bộ cuối")
    private LocalDateTime lastSyncedAt;
    
    @Schema(description = "Thời gian tạo đơn")
    private LocalDateTime createdAt;
    
    @Schema(description = "Thời gian cập nhật")
    private LocalDateTime updatedAt;
    
    @Schema(description = "Danh sách sản phẩm trong đơn")
    private List<OrderItemDto> orderItems;
}
