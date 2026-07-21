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
@Schema(description = "Thông tin sản phẩm sàn KiotViet kèm các cấu hình ánh xạ trứng")
public class KiotvietProductDto {
    @Schema(description = "ID sản phẩm trên KiotViet")
    private Long kvProductId;
    
    @Schema(description = "Mã sản phẩm (SKU)")
    private String code;
    
    @Schema(description = "Tên sản phẩm")
    private String name;
    
    @Schema(description = "Tên đầy đủ")
    private String fullName;
    
    @Schema(description = "Giá gốc sản phẩm")
    private Double basePrice;
    
    @Schema(description = "Ảnh đại diện sản phẩm")
    private String imageUrl;
    
    @Schema(description = "Thời gian đồng bộ cuối cùng")
    private LocalDateTime lastSyncedAt;
    
    @Schema(description = "Số lượng trứng thường (Loại 1) phát cho mỗi sản phẩm")
    private Integer eggType1Qty;

    @Schema(description = "Số lượng trứng ấp (Loại 2) phát cho mỗi sản phẩm")
    private Integer eggType2Qty;

    @Schema(description = "Danh sách mapping cấu hình đẻ trứng")
    private List<ProductMappingDto> mappings;
}
