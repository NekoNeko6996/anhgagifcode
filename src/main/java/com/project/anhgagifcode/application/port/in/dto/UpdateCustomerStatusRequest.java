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
@Schema(description = "Yêu cầu thay đổi trạng thái khách hàng")
public class UpdateCustomerStatusRequest {

    @NotBlank(message = "Trạng thái không được để trống")
    @Schema(description = "Trạng thái mới (NEW, WARNING, BANNED, TRUSTED_1, TRUSTED_2)")
    private String status;

    @Schema(description = "Số lần hoàn trả liên tiếp (nếu cần điều chỉnh, ví dụ reset về 0)")
    private Integer returnStreak;

    @Schema(description = "Số đơn hàng giao thành công")
    private Integer successCount;

    @Schema(description = "Ngày gỡ khóa tài khoản")
    private java.time.LocalDateTime unbanAt;
}
