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
@Schema(description = "Thông tin chi tiết của quả trứng và các quan hệ liên quan")
public class EggDto {
    @Schema(description = "ID trứng")
    private String id;
    
    @Schema(description = "Loại trứng (1: Thường, 2: Cần ấp)")
    private int eggType;
    
    @Schema(description = "Trạng thái (PENDING, CLAIMED, CANCELLED)")
    private String status;
    
    @Schema(description = "Thời gian ấp nở dự kiến")
    private LocalDateTime hatchAt;
    
    @Schema(description = "Thời gian tạo")
    private LocalDateTime createdAt;
    
    @Schema(description = "Thời gian cập nhật")
    private LocalDateTime updatedAt;
    
    @Schema(description = "Thông tin đơn hàng liên quan")
    private OrderSummaryDto order;
    
    @Schema(description = "Thông tin tài khoản quà đã được gán (nếu có)")
    private GiftAccountSummaryDto account;
    
    @Schema(description = "Thông tin bể quà (Gift Pool) liên kết")
    private GiftPoolSummaryDto giftPool;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Thông tin đơn hàng rút gọn")
    public static class OrderSummaryDto {
        private String id;
        private String orderCode;
        private String deliveryStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Thông tin tài khoản quà tặng rút gọn")
    public static class GiftAccountSummaryDto {
        private String id;
        private String username;
        private String platform;
        private String tier;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Thông tin bể quà rút gọn")
    public static class GiftPoolSummaryDto {
        private String id;
        private String poolName;
        private String tier;
    }
}
