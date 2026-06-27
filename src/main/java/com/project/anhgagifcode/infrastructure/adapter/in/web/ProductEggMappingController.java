package com.project.anhgagifcode.infrastructure.adapter.in.web;

import com.project.anhgagifcode.application.port.in.DeleteProductEggMappingUseCase;
import com.project.anhgagifcode.application.port.in.LinkProductToEggUseCase;
import com.project.anhgagifcode.application.port.in.dto.BatchDeleteMappingRequest;
import com.project.anhgagifcode.application.port.in.dto.LinkProductToEggRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/product-egg-mappings")
@RequiredArgsConstructor
@Tag(name = "Admin - Ánh xạ Sản phẩm - Trứng", description = "Các API cấu hình ánh xạ sản phẩm Kiotviet sang loại trứng của bể quà (Yêu cầu JWT Token)")
@SecurityRequirement(name = "bearerAuth")
public class ProductEggMappingController {

    private final LinkProductToEggUseCase linkProductToEggUseCase;
    private final DeleteProductEggMappingUseCase deleteProductEggMappingUseCase;

    @Operation(summary = "Liên kết sản phẩm Kiotviet với loại trứng trong bể quà", description = "Tạo hoặc cập nhật liên kết giữa sản phẩm Kiotviet với loại trứng (1 hoặc 2) của một bể quà chỉ định. Tối đa 2 liên kết (2 loại trứng) cho mỗi sản phẩm.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liên kết thành công"),
            @ApiResponse(responseCode = "400", description = "Vượt quá số lượng liên kết tối đa hoặc dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm hoặc bể quà"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền")
    })
    @PostMapping
    public ResponseEntity<Map<String, String>> linkProductToEgg(@Valid @RequestBody LinkProductToEggRequest request) {
        linkProductToEggUseCase.linkProductToEgg(request);
        return ResponseEntity.ok(Map.of("message", "Liên kết sản phẩm với trứng thành công!"));
    }

    @Operation(summary = "Xóa một liên kết sản phẩm - trứng", description = "Xóa một quy tắc liên kết sản phẩm - trứng theo ID liên kết.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Xóa liên kết thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy liên kết quy định"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteMapping(@PathVariable("id") String mappingId) {
        deleteProductEggMappingUseCase.deleteMapping(mappingId);
        return ResponseEntity.ok(Map.of("message", "Xóa liên kết thành công!"));
    }

    @Operation(summary = "Xóa hàng loạt liên kết sản phẩm - trứng", description = "Xóa danh sách các quy tắc liên kết sản phẩm - trứng theo danh sách ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Xóa các liên kết thành công"),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền")
    })
    @PostMapping("/batch-delete")
    public ResponseEntity<Map<String, String>> batchDeleteMappings(@Valid @RequestBody BatchDeleteMappingRequest request) {
        deleteProductEggMappingUseCase.deleteMappings(request);
        return ResponseEntity.ok(Map.of("message", "Xóa danh sách liên kết thành công!"));
    }
}
