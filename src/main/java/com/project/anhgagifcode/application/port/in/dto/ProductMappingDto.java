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
@Schema(description = "Cấu hình ánh xạ sản phẩm sang trứng")
public class ProductMappingDto {
    @Schema(description = "ID cấu hình mapping")
    private String id;
    
    @Schema(description = "Cấp độ trứng (Egg Tier)")
    private String eggTier;
    
    @Schema(description = "Bể quà (Gift Pool) nhận tương ứng")
    private GiftPoolDto giftPool;
    
    @Schema(description = "Tỉ lệ nhận trứng (%)")
    private double rate;
    
    @Schema(description = "Thời gian tạo")
    private LocalDateTime createdAt;
    
    @Schema(description = "Thời gian cập nhật")
    private LocalDateTime updatedAt;
}
