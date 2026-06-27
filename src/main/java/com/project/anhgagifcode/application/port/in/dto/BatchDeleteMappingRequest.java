package com.project.anhgagifcode.application.port.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu xóa danh sách liên kết sản phẩm - trứng")
public class BatchDeleteMappingRequest {
    
    @NotEmpty(message = "Danh sách ID liên kết không được để trống")
    @Schema(description = "Danh sách ID liên kết cần xóa")
    private List<String> mappingIds;
}
