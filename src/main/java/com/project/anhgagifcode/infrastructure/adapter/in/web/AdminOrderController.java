package com.project.anhgagifcode.infrastructure.adapter.in.web;

import com.project.anhgagifcode.application.port.in.GetKiotvietOrdersUseCase;
import com.project.anhgagifcode.application.port.in.dto.KiotvietOrderDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@Tag(name = "Admin - Quản lý Đơn hàng", description = "Các API truy vấn thông tin Đơn hàng KiotViet (Yêu cầu JWT Token)")
@SecurityRequirement(name = "bearerAuth")
public class AdminOrderController {

    private final GetKiotvietOrdersUseCase getKiotvietOrdersUseCase;

    @Operation(summary = "Lấy danh sách tất cả các đơn hàng", description = "Trả về danh sách toàn bộ đơn hàng kèm các mặt hàng (items) mua trong đơn.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền")
    })
    @GetMapping
    public ResponseEntity<List<KiotvietOrderDto>> getAllOrders() {
        return ResponseEntity.ok(getKiotvietOrdersUseCase.getOrders());
    }
}
