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
@Schema(description = "Thông tin bể quà tặng (Gift Pool)")
public class GiftPoolDto {
    @Schema(description = "ID bể quà")
    private String id;
    
    @Schema(description = "Tên bể quà")
    private String poolName;
    
    @Schema(description = "Phân cấp (Tier)")
    private String tier;
    
    @Schema(description = "Thời gian tạo")
    private LocalDateTime createdAt;
}
