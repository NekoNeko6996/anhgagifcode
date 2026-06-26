package com.project.anhgagifcode.infrastructure.adapter.in.web;

import com.project.anhgagifcode.application.port.in.ClaimEggUseCase;
import com.project.anhgagifcode.application.port.in.SyncKiotvietOrderUseCase;
import com.project.anhgagifcode.application.port.in.dto.ClaimEggResponse;
import com.project.anhgagifcode.application.port.in.dto.SyncOrderResponse;
import com.project.anhgagifcode.infrastructure.adapter.in.web.dto.ClaimEggRequest;
import com.project.anhgagifcode.infrastructure.adapter.in.web.dto.SyncOrderRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/eggs")
@RequiredArgsConstructor
@Tag(name = "User - Hệ thống Trứng", description = "API cho khách hàng tra cứu đơn và mở trứng nhận quà")
public class EggController {

    private final SyncKiotvietOrderUseCase syncUseCase;
    private final ClaimEggUseCase claimUseCase;

    @Operation(summary = "Nhập Code Đơn Hàng", description = "Đồng bộ đơn KiotViet, tính toán logic Uy tín/Ban và trả về danh sách Trứng. Giới hạn: 3 lần/phút.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công, trả về danh sách trứng"),
            @ApiResponse(responseCode = "400", description = "Mã đơn không hợp lệ hoặc khách hàng bị BAN"),
            @ApiResponse(responseCode = "429", description = "Thao tác quá nhanh (Spam)")
    })
    @PostMapping("/sync")
    public ResponseEntity<SyncOrderResponse> syncOrder(
            @Valid @RequestBody SyncOrderRequest request) { // @Valid cực kỳ quan trọng để kích hoạt Validation DTO
        
        SyncOrderResponse response = syncUseCase.syncAndGetOrderDetails(request.getOrderCode());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Bấm Mở Trứng (Claim)", description = "Yêu cầu mở 1 quả trứng dựa vào eggId. Hệ thống sẽ bốc thăm ngẫu nhiên 1 tài khoản VIP. Giới hạn: 3 lần/phút.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mở thành công, trả về Account và Password"),
            @ApiResponse(responseCode = "400", description = "Trứng chưa sẵn sàng, hết quà, hoặc trứng đã mở"),
            @ApiResponse(responseCode = "429", description = "Thao tác quá nhanh (Spam)")
    })
    @PostMapping("/claim")
    public ResponseEntity<ClaimEggResponse> claimEgg(
            @Valid @RequestBody ClaimEggRequest request,
            HttpServletRequest httpRequest) {
        
        // Lấy IP để ghi Log hệ thống
        String ipAddress = getClientIp(httpRequest);
        
        ClaimEggResponse response = claimUseCase.claimEggReward(request.getEggId(), ipAddress);
        return ResponseEntity.ok(response);
    }

    // Tiện ích lấy IP cục bộ (tương tự trong Interceptor)
    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}