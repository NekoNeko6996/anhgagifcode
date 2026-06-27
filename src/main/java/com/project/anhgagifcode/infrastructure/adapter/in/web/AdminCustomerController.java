package com.project.anhgagifcode.infrastructure.adapter.in.web;

import com.project.anhgagifcode.application.port.in.GetCustomersUseCase;
import com.project.anhgagifcode.application.port.in.dto.CustomerDto;
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
@RequestMapping("/api/admin/customers")
@RequiredArgsConstructor
@Tag(name = "Admin - Quản lý Khách hàng", description = "Các API truy vấn thông tin Khách hàng (Yêu cầu JWT Token)")
@SecurityRequirement(name = "bearerAuth")
public class AdminCustomerController {

    private final GetCustomersUseCase getCustomersUseCase;

    @Operation(summary = "Lấy danh sách tất cả khách hàng", description = "Trả về toàn bộ danh sách khách hàng cùng trạng thái và uy tín.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền")
    })
    @GetMapping
    public ResponseEntity<List<CustomerDto>> getAllCustomers() {
        return ResponseEntity.ok(getCustomersUseCase.getCustomers());
    }
}
