package com.project.anhgagifcode.infrastructure.adapter.in.web;

import com.project.anhgagifcode.application.port.in.GetCustomersUseCase;
import com.project.anhgagifcode.application.port.in.UpdateCustomerStatusUseCase;
import com.project.anhgagifcode.application.port.in.dto.CustomerDto;
import com.project.anhgagifcode.application.port.in.dto.UpdateCustomerStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/customers")
@RequiredArgsConstructor
@Tag(name = "Admin - Quản lý Khách hàng", description = "Các API truy vấn và quản lý trạng thái Khách hàng (Yêu cầu JWT Token)")
@SecurityRequirement(name = "bearerAuth")
public class AdminCustomerController {

    private final GetCustomersUseCase getCustomersUseCase;
    private final UpdateCustomerStatusUseCase updateCustomerStatusUseCase;

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

    @Operation(summary = "Cập nhật trạng thái khách hàng thủ công", description = "Cho phép admin cập nhật trực tiếp status (NEW, WARNING, BANNED) và chỉ số vi phạm hoàn hàng của khách hàng.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy khách hàng"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền")
    })
    @PutMapping("/{customerCode}/status")
    public ResponseEntity<CustomerDto> updateCustomerStatus(
            @PathVariable("customerCode") String customerCode,
            @Valid @RequestBody UpdateCustomerStatusRequest request) {
        CustomerDto updated = updateCustomerStatusUseCase.updateCustomerStatus(customerCode, request);
        return ResponseEntity.ok(updated);
    }
}
