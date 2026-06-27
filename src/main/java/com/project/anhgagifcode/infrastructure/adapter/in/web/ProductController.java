/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.project.anhgagifcode.infrastructure.adapter.in.web;

import com.project.anhgagifcode.application.port.in.SyncKiotvietProductUseCase;
import com.project.anhgagifcode.application.port.in.GetKiotvietProductsUseCase;
import com.project.anhgagifcode.application.port.in.dto.KiotvietProductDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@Tag(name = "Admin - Quản lý sản phẩm", description = "Các API Đồng bộ và quản lý sản phẩm được đồng bộ từ sàn") 
@SecurityRequirement(name = "bearerAuth")
public class ProductController {
    
    private final SyncKiotvietProductUseCase syncKiotvietProductUseCase;
    private final GetKiotvietProductsUseCase getKiotvietProductsUseCase;
    
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
}
