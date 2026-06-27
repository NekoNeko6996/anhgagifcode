package com.project.anhgagifcode.infrastructure.adapter.in.web;

import com.project.anhgagifcode.application.port.in.AddGiftAccountUseCase;
import com.project.anhgagifcode.application.port.in.DeleteGiftAccountsUseCase;
import com.project.anhgagifcode.application.port.in.GetGiftAccountsUseCase;
import com.project.anhgagifcode.application.port.in.dto.DeleteGiftAccountsRequest;
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
    private final DeleteGiftAccountsUseCase deleteGiftAccountsUseCase;

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

    @Operation(summary = "Xóa danh sách tài khoản quà tặng", description = "Xóa một hoặc nhiều tài khoản quà tặng khỏi kho, tự động gỡ liên kết khỏi các bể quà và dọn sạch lịch sử mở trứng.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Xóa tài khoản thành công"),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền")
    })
    @PostMapping("/batch-delete")
    public ResponseEntity<Map<String, String>> batchDeleteAccounts(
            @Valid @RequestBody DeleteGiftAccountsRequest request) {
        deleteGiftAccountsUseCase.deleteAccounts(request);
        return ResponseEntity.ok(Map.of("message", "Xóa các tài khoản thành công!"));
    }

    private final com.project.anhgagifcode.application.port.in.UpdateGiftAccountUseCase updateGiftAccountUseCase;

    @Operation(summary = "Cập nhật thông tin tài khoản quà tặng", description = "Cho phép admin chỉnh sửa chi tiết tài khoản quà tặng (username, password, platform, status, token).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy tài khoản"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền")
    })
    @PutMapping("/{id}")
    public ResponseEntity<GiftAccountDto> updateGiftAccount(
            @PathVariable("id") String accountId,
            @Valid @RequestBody com.project.anhgagifcode.application.port.in.dto.UpdateGiftAccountRequest request) {
        GiftAccountDto updated = updateGiftAccountUseCase.updateGiftAccount(accountId, request);
        return ResponseEntity.ok(updated);
    }
}