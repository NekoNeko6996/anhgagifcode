package com.project.anhgagifcode.infrastructure.adapter.in.web;

import com.project.anhgagifcode.application.port.in.GetEggsUseCase;
import com.project.anhgagifcode.application.port.in.UpdateEggHatchTimeUseCase;
import com.project.anhgagifcode.application.port.in.GetEarlyHatchEligibleUseCase;
import com.project.anhgagifcode.application.port.in.ApproveEarlyHatchUseCase;
import com.project.anhgagifcode.application.port.in.dto.EarlyHatchGroupDto;
import com.project.anhgagifcode.application.port.in.dto.EggDto;
import com.project.anhgagifcode.application.port.in.dto.UpdateHatchTimeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/eggs")
@RequiredArgsConstructor
@Tag(name = "Admin - Quản lý Trứng", description = "Các API truy vấn thông tin Trứng (Yêu cầu JWT Token)")
@SecurityRequirement(name = "bearerAuth")
public class AdminEggController {

    private final GetEggsUseCase getEggsUseCase;
    private final UpdateEggHatchTimeUseCase updateEggHatchTimeUseCase;
    private final GetEarlyHatchEligibleUseCase getEarlyHatchEligibleUseCase;
    private final ApproveEarlyHatchUseCase approveEarlyHatchUseCase;

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

    @Operation(summary = "Cập nhật thời gian nở của trứng", description = "Cho phép admin chỉnh sửa thời gian nở (hatchAt) của quả trứng.")
    @PutMapping("/{id}/hatch-time")
    public ResponseEntity<Void> updateHatchTime(
            @PathVariable("id") String eggId,
            @RequestBody UpdateHatchTimeRequest request) {
        updateEggHatchTimeUseCase.updateHatchTime(eggId, request.getHatchAt());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Lấy danh sách trứng đủ điều kiện duyệt sớm", description = "Lấy danh sách trứng đang ấp của đơn nhiều sản phẩm/số lượng lớn và khách hàng có tín dụng duyệt sớm.")
    @GetMapping("/early-hatch/eligible")
    public ResponseEntity<List<EarlyHatchGroupDto>> getEarlyHatchEligible() {
        return ResponseEntity.ok(getEarlyHatchEligibleUseCase.getEligibleItems());
    }

    @Operation(summary = "Duyệt sớm 3 ngày cho trứng (Trừ 1 tín dụng)", description = "Khấu trừ 1 early_hatch_credits của khách hàng và giảm 3 ngày ấp của quả trứng.")
    @PostMapping("/{id}/reduce-hatch-time-manual")
    public ResponseEntity<Void> approveEarlyHatch(@PathVariable("id") String eggId) {
        approveEarlyHatchUseCase.approveEarlyHatch(eggId);
        return ResponseEntity.ok().build();
    }
}
