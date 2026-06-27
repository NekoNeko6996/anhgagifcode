package com.project.anhgagifcode.infrastructure.adapter.in.web;

import com.project.anhgagifcode.application.port.in.AddAccountToPoolUseCase;
import com.project.anhgagifcode.application.port.in.AddAccountsToPoolUseCase;
import com.project.anhgagifcode.application.port.in.CreateGiftPoolUseCase;
import com.project.anhgagifcode.application.port.in.GetGiftPoolDetailUseCase;
import com.project.anhgagifcode.application.port.in.GetGiftPoolsUseCase;
import com.project.anhgagifcode.application.port.in.RemoveAccountsFromPoolUseCase;
import com.project.anhgagifcode.application.port.in.RemoveGiftPoolUseCase;
import com.project.anhgagifcode.application.port.in.UpdateGiftPoolUseCase;
import com.project.anhgagifcode.application.port.in.dto.AddAccountToPoolRequest;
import com.project.anhgagifcode.application.port.in.dto.AddAccountsToPoolRequest;
import com.project.anhgagifcode.application.port.in.dto.CreateGiftPoolRequest;
import com.project.anhgagifcode.application.port.in.dto.GiftPoolDetailDto;
import com.project.anhgagifcode.application.port.in.dto.GiftPoolDto;
import com.project.anhgagifcode.application.port.in.dto.RemoveAccountsFromPoolRequest;
import com.project.anhgagifcode.application.port.in.dto.UpdateGiftPoolRequest;
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
import java.util.Map;

@RestController
@RequestMapping("/api/admin/gift-pools")
@RequiredArgsConstructor
@Tag(name = "Admin - Quản lý Bể Quà", description = "Các API truy vấn và quản lý Bể Quà - Gift Pool (Yêu cầu JWT Token)")
@SecurityRequirement(name = "bearerAuth")
public class AdminGiftPoolController {

    private final GetGiftPoolsUseCase getGiftPoolsUseCase;
    private final CreateGiftPoolUseCase createGiftPoolUseCase;
    private final RemoveGiftPoolUseCase removeGiftPoolUseCase;
    private final AddAccountToPoolUseCase addAccountToPoolUseCase;
    private final AddAccountsToPoolUseCase addAccountsToPoolUseCase;
    private final GetGiftPoolDetailUseCase getGiftPoolDetailUseCase;
    private final UpdateGiftPoolUseCase updateGiftPoolUseCase;
    private final RemoveAccountsFromPoolUseCase removeAccountsFromPoolUseCase;

    @Operation(summary = "Lấy danh sách tất cả bể quà", description = "Trả về danh sách toàn bộ bể quà (Gift Pool) hiện có trong hệ thống.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền")
    })
    @GetMapping
    public ResponseEntity<List<GiftPoolDto>> getAllGiftPools() {
        return ResponseEntity.ok(getGiftPoolsUseCase.getGiftPools());
    }

    @Operation(summary = "Lấy chi tiết bể quà", description = "Trả về thông tin chi tiết của một bể quà bao gồm danh sách các tài khoản quà tặng đã gán.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy chi tiết thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bể quà"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền")
    })
    @GetMapping("/{id}")
    public ResponseEntity<GiftPoolDetailDto> getGiftPoolDetail(@PathVariable("id") String poolId) {
        return ResponseEntity.ok(getGiftPoolDetailUseCase.getPoolDetail(poolId));
    }

    @Operation(summary = "Tạo bể quà mới", description = "Tạo một bể quà mới với tên và tier (A, B, C, D...) được chỉ định.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tạo thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc sai định dạng tier"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền")
    })
    @PostMapping
    public ResponseEntity<GiftPoolDto> createGiftPool(@Valid @RequestBody CreateGiftPoolRequest request) {
        return ResponseEntity.ok(createGiftPoolUseCase.createPool(request));
    }

    @Operation(summary = "Chỉnh sửa thông tin bể quà", description = "Chỉnh sửa tên và tier (A, B, C, D...) của bể quà theo ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc sai định dạng tier"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bể quà"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền")
    })
    @PutMapping("/{id}")
    public ResponseEntity<GiftPoolDto> updateGiftPool(
            @PathVariable("id") String poolId,
            @Valid @RequestBody UpdateGiftPoolRequest request) {
        return ResponseEntity.ok(updateGiftPoolUseCase.updatePool(poolId, request));
    }

    @Operation(summary = "Xóa bể quà", description = "Xóa bể quà theo ID. Lưu ý: Chỉ được phép xóa khi bể quà không chứa trứng liên kết.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @ApiResponse(responseCode = "400", description = "Bể quà đang chứa trứng liên kết, không thể xóa"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bể quà"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteGiftPool(@PathVariable("id") String poolId) {
        removeGiftPoolUseCase.removePool(poolId);
        return ResponseEntity.ok(Map.of("message", "Xóa bể quà thành công!"));
    }

    @Operation(summary = "Thêm tài khoản vào bể quà", description = "Gán một tài khoản quà tặng hiện có vào một bể quà.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Gán thành công"),
            @ApiResponse(responseCode = "400", description = "Tài khoản đã nằm trong bể quà này hoặc lỗi ràng buộc"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bể quà hoặc tài khoản"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền")
    })
    @PostMapping("/add-account")
    public ResponseEntity<Map<String, String>> addAccountToPool(@Valid @RequestBody AddAccountToPoolRequest request) {
        addAccountToPoolUseCase.addAccountToPool(request);
        return ResponseEntity.ok(Map.of("message", "Thêm tài khoản vào bể quà thành công!"));
    }

    @Operation(summary = "Thêm nhiều tài khoản vào bể quà", description = "Gán danh sách các tài khoản quà tặng hiện có vào một bể quà một lúc. Tự động bỏ qua các tài khoản đã được gán trước đó.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Gán thành công"),
            @ApiResponse(responseCode = "400", description = "Lỗi ràng buộc dữ liệu đầu vào"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bể quà hoặc một trong các tài khoản"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền")
    })
    @PostMapping("/add-accounts")
    public ResponseEntity<Map<String, String>> addAccountsToPool(@Valid @RequestBody AddAccountsToPoolRequest request) {
        addAccountsToPoolUseCase.addAccountsToPool(request);
        return ResponseEntity.ok(Map.of("message", "Thêm danh sách tài khoản vào bể quà thành công!"));
    }

    @Operation(summary = "Xóa liên kết của một hoặc nhiều tài khoản khỏi bể quà", description = "Gỡ một hoặc danh sách tài khoản quà tặng khỏi bể quà đã gán.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Xóa liên kết thành công"),
            @ApiResponse(responseCode = "400", description = "Lỗi dữ liệu đầu vào không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bể quà"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền")
    })
    @PostMapping("/remove-accounts")
    public ResponseEntity<Map<String, String>> removeAccountsFromPool(@Valid @RequestBody RemoveAccountsFromPoolRequest request) {
        removeAccountsFromPoolUseCase.removeAccountsFromPool(request);
        return ResponseEntity.ok(Map.of("message", "Xóa liên kết các tài khoản khỏi bể quà thành công!"));
    }
}
