package com.project.anhgagifcode.infrastructure.adapter.in.web;

import com.project.anhgagifcode.application.port.in.SyncSapoOrderUseCase;
import com.project.anhgagifcode.domain.model.SapoOrder;
import com.project.anhgagifcode.infrastructure.adapter.in.web.dto.SapoWebhookRequest;
import com.project.anhgagifcode.infrastructure.adapter.in.web.exception.ErrorResponse;
import com.project.anhgagifcode.infrastructure.adapter.in.web.mapper.SapoWebhookMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/webhook/orders")
@RequiredArgsConstructor
@Tag(name = "Sapo Webhook", description = "Các API dùng để nhận dữ liệu sự kiện từ hệ thống Sapo")
public class SapoWebhookController {

    private final SyncSapoOrderUseCase syncSapoOrderUseCase;
    private final SapoWebhookMapper webhookMapper;

    @Operation(summary = "Nhận Webhook Đơn Hàng", description = "Sapo tự động gọi endpoint này khi có sự kiện: tạo đơn, thanh toán, huỷ đơn.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đã nhận và xử lý thành công"),
            @ApiResponse(responseCode = "400", description = "Payload không hợp lệ / Vi phạm logic", 
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống nội bộ", 
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<Void> handleOrderWebhook(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "JSON Payload chuẩn của Sapo Webhook", required = true)
            @RequestBody SapoWebhookRequest request) {
        
        log.info("Nhận webhook đơn hàng từ Sapo: {}", request.getOrderCode());
        
        SapoOrder incomingOrder = webhookMapper.toDomain(request);
        syncSapoOrderUseCase.syncOrder(incomingOrder);
        
        return ResponseEntity.ok().build();
    }
}