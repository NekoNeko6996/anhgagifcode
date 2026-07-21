package com.project.anhgagifcode.infrastructure.adapter.in.web;

import com.project.anhgagifcode.application.port.in.SyncKiotvietProductUseCase;
import com.project.anhgagifcode.application.port.in.GetKiotvietProductsUseCase;
import com.project.anhgagifcode.application.port.in.UpdateProductEggQuantitiesUseCase;
import com.project.anhgagifcode.application.port.in.dto.KiotvietProductDto;
import com.project.anhgagifcode.infrastructure.adapter.in.web.dto.UpdateEggQuantitiesRequest;
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
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@Tag(name = "Admin - Quản lý sản phẩm", description = "Các API Đồng bộ và quản lý sản phẩm được đồng bộ từ sàn") 
@SecurityRequirement(name = "bearerAuth")
public class ProductController {
    
    private final SyncKiotvietProductUseCase syncKiotvietProductUseCase;
    private final GetKiotvietProductsUseCase getKiotvietProductsUseCase;
    private final UpdateProductEggQuantitiesUseCase updateProductEggQuantitiesUseCase;
    
    @Operation(summary = "Lấy danh sách tất cả sản phẩm", description = "Trả về toàn bộ danh sách sản phẩm đồng bộ từ sàn và các cấu hình ánh xạ trứng tương ứng.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền")
    })
    @GetMapping
    public ResponseEntity<List<KiotvietProductDto>> getAllProducts() {
        return ResponseEntity.ok(getKiotvietProductsUseCase.getProducts());
    }

    @Operation(summary = "Đồng bộ toàn bộ danh sách sản phẩm", description = "gọi API để đồng bộ thủ công, danh sách sẽ được cập nhật, thêm mới chứ không bị xóa.")
    @PostMapping("/sync/all")
    public ResponseEntity<Integer> syncAllProducts() {
        int rowAdded = syncKiotvietProductUseCase.syncProductsFromKiotviet();
        return ResponseEntity.ok(rowAdded);
    }

    @Operation(summary = "Cấu hình số lượng trứng cho sản phẩm", description = "Cập nhật số lượng trứng Thường (Loại 1) và trứng Ấp (Loại 2) phát cho sản phẩm.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền")
    })
    @PutMapping("/{productId}/egg-quantities")
    public ResponseEntity<Map<String, String>> updateEggQuantities(
            @PathVariable("productId") Long productId,
            @Valid @RequestBody UpdateEggQuantitiesRequest request) {
        updateProductEggQuantitiesUseCase.updateEggQuantities(productId, request.getEggType1Qty(), request.getEggType2Qty());
        return ResponseEntity.ok(Map.of("message", "Cập nhật số lượng trứng phát thành công!"));
    }
}
