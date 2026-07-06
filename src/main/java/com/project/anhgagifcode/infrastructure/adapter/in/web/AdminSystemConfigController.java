package com.project.anhgagifcode.infrastructure.adapter.in.web;

import com.project.anhgagifcode.application.port.out.SystemConfigPersistencePort;
import com.project.anhgagifcode.domain.model.SystemConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/system-configs")
@RequiredArgsConstructor
@Tag(name = "Admin - Cấu hình hệ thống", description = "Các API quản lý cấu hình hệ thống (Yêu cầu JWT Token)")
@SecurityRequirement(name = "bearerAuth")
public class AdminSystemConfigController {

    private final SystemConfigPersistencePort configPort;

    @Operation(summary = "Lấy tất cả cấu hình hệ thống")
    @GetMapping
    public ResponseEntity<List<SystemConfig>> getAllConfigs() {
        return ResponseEntity.ok(configPort.findAll());
    }

    @Operation(summary = "Cập nhật cấu hình hệ thống")
    @PostMapping
    public ResponseEntity<Map<String, String>> updateConfigs(@RequestBody Map<String, String> updates) {
        for (Map.Entry<String, String> entry : updates.entrySet()) {
            Optional<SystemConfig> configOpt = configPort.findByKey(entry.getKey());
            if (configOpt.isPresent()) {
                SystemConfig config = configOpt.get();
                config.setConfigValue(entry.getValue());
                config.setUpdatedAt(java.time.LocalDateTime.now());
                configPort.save(config);
            } else {
                SystemConfig config = SystemConfig.builder()
                        .configKey(entry.getKey())
                        .configValue(entry.getValue())
                        .updatedAt(java.time.LocalDateTime.now())
                        .build();
                configPort.save(config);
            }
        }
        return ResponseEntity.ok(Map.of("message", "Cập nhật cấu hình hệ thống thành công!"));
    }
}
