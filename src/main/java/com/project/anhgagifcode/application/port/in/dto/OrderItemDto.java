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
@Schema(description = "Chi tiết mặt hàng trong đơn hàng KiotViet")
public class OrderItemDto {
    @Schema(description = "ID chi tiết")
    private String id;
    
    @Schema(description = "ID sản phẩm KiotViet")
    private String kvProductId;
    
    @Schema(description = "Số lượng")
    private int quantity;
    
    @Schema(description = "Thời gian đồng bộ")
    private LocalDateTime lastSyncedAt;
}
