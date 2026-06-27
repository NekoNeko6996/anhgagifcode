package com.project.anhgagifcode.infrastructure.adapter.in.web;

import com.project.anhgagifcode.application.port.in.AddGiftAccountUseCase;
import com.project.anhgagifcode.application.port.in.GetGiftAccountsUseCase;
import com.project.anhgagifcode.application.port.in.dto.GiftAccountDto;
import com.project.anhgagifcode.infrastructure.adapter.in.web.dto.CreateGiftAccountRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/admin/gift-accounts")
@RequiredArgsConstructor
@Tag(name = "Admin - Quản lý Kho Quà", description = "Các API thêm và quản lý Gift Accounts (Yêu cầu JWT Token)")
@SecurityRequirement(name = "bearerAuth")
public class AdminGiftAccountController {

    private final AddGiftAccountUseCase addGiftAccountUseCase;
    private final GetGiftAccountsUseCase getGiftAccountsUseCase;

    @Operation(summary = "Lấy danh sách tất cả tài khoản quà tặng", description = "Trả về toàn bộ danh sách tài khoản quà tặng hiện có trong kho.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền")
    })
    @GetMapping
    public ResponseEntity<List<GiftAccountDto>> getAllGiftAccounts() {
        return ResponseEntity.ok(getGiftAccountsUseCase.getGiftAccounts());
    }

    @Operation(summary = "Thêm 1 tài khoản", description = "Nhập tay từng tài khoản vào kho quà")
    @PostMapping("/single")
    public ResponseEntity<Map<String, String>> addSingleAccount(
            @Valid @RequestBody CreateGiftAccountRequest request) {
        
        addGiftAccountUseCase.addSingleAccount(request);
        return ResponseEntity.ok(Map.of("message", "Thêm tài khoản thành công!"));
    }

    @Operation(summary = "Import tài khoản từ Excel", description = "Tải lên file Excel (.xlsx) với cấu trúc chuẩn để import hàng loạt")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadExcel(
            @Parameter(description = "File Excel chứa danh sách tài khoản") 
            @RequestParam("file") MultipartFile file) {
        
        int count = addGiftAccountUseCase.importAccountsFromExcel(file);
        return ResponseEntity.ok(Map.of(
                "message", "Import dữ liệu thành công!",
                "totalImported", count
        ));
    }
}