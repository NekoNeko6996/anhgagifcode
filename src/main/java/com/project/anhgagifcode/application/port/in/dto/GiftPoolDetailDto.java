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
@Schema(description = "Thông tin chi tiết bể quà")
public class GiftPoolDetailDto {
    
    @Schema(description = "ID bể quà")
    private String id;

    @Schema(description = "Tên bể quà")
    private String poolName;

    @Schema(description = "Tier của bể quà (A, B, C, D...)")
    private String tier;

    @Schema(description = "Ngày tạo")
    private LocalDateTime createdAt;

    @Schema(description = "Danh sách các tài khoản quà tặng thuộc bể quà này")
    private List<GiftAccountDto> accounts;
}
