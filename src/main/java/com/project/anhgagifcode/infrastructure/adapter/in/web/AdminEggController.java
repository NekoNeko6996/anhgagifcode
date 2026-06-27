package com.project.anhgagifcode.infrastructure.adapter.in.web;

import com.project.anhgagifcode.application.port.in.GetEggsUseCase;
import com.project.anhgagifcode.application.port.in.dto.EggDto;
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
@RequestMapping("/api/admin/eggs")
@RequiredArgsConstructor
@Tag(name = "Admin - Quản lý Trứng", description = "Các API truy vấn thông tin Trứng (Yêu cầu JWT Token)")
@SecurityRequirement(name = "bearerAuth")
public class AdminEggController {

    private final GetEggsUseCase getEggsUseCase;

    @Operation(summary = "Lấy danh sách tất cả các quả trứng", description = "Trả về danh sách toàn bộ trứng trong hệ thống kèm thông tin chi tiết về đơn hàng, tài khoản quà và bể quà liên quan.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền")
    })
    @GetMapping
    public ResponseEntity<List<EggDto>> getAllEggs() {
        return ResponseEntity.ok(getEggsUseCase.getEggs());
    }
}
