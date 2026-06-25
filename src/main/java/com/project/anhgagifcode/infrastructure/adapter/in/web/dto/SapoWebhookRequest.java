package com.project.anhgagifcode.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Payload đơn hàng gửi từ Sapo Webhook")
public class SapoWebhookRequest {

    @Schema(description = "Mã đơn hàng", example = "SON10001")
    @JsonProperty("order_code")
    private String orderCode;

    @Schema(description = "Nguồn tạo đơn", example = "web")
    @JsonProperty("source_name")
    private String sourceName;

    @Schema(description = "Tổng giá trị đơn hàng", example = "500000.00")
    @JsonProperty("total_price")
    private BigDecimal totalPrice;

    @Schema(description = "Trạng thái thanh toán", example = "paid")
    @JsonProperty("financial_status")
    private String financialStatus;

    @Schema(description = "Trạng thái giao hàng", example = "pending")
    @JsonProperty("fulfillment_status")
    private String fulfillmentStatus;

    @Schema(description = "Trạng thái chung của đơn", example = "open")
    @JsonProperty("status")
    private String status;

    @Schema(description = "Thời gian tạo (ISO-8601)", example = "2026-06-25T10:00:00Z")
    @JsonProperty("created_on")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssX")
    private LocalDateTime createdAt;

    @Schema(description = "Thời gian cập nhật (ISO-8601)", example = "2026-06-25T10:15:00Z")
    @JsonProperty("modified_on")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssX")
    private LocalDateTime updatedAt;

    @Schema(description = "Danh sách sản phẩm trong đơn")
    @JsonProperty("line_items")
    private List<SapoWebhookItemRequest> lineItems;

    @Data
    @Schema(description = "Chi tiết từng sản phẩm")
    public static class SapoWebhookItemRequest {
        @Schema(description = "ID sản phẩm trên Sapo", example = "123456789")
        @JsonProperty("product_id")
        private String sapoProductId;

        @Schema(description = "ID phiên bản (variant) trên Sapo", example = "987654321")
        @JsonProperty("variant_id")
        private String sapoVariantId;

        @Schema(description = "Mã SKU sản phẩm", example = "PROD-VANG-01")
        @JsonProperty("sku")
        private String sku;

        @Schema(description = "Số lượng mua", example = "2")
        @JsonProperty("quantity")
        private int quantity;
    }
}