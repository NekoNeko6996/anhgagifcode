package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.UpdateMappingRatesUseCase;
import com.project.anhgagifcode.application.port.in.dto.UpdateMappingRateRequest;
import com.project.anhgagifcode.application.port.out.ProductEggMappingPersistencePort;
import com.project.anhgagifcode.domain.exception.BusinessRuleViolationException;
import com.project.anhgagifcode.domain.model.ProductEggMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class UpdateMappingRatesService implements UpdateMappingRatesUseCase {

    private final ProductEggMappingPersistencePort mappingPersistencePort;

    @Override
    @Transactional
    public void updateRates(Long productId, List<UpdateMappingRateRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new BusinessRuleViolationException("Danh sách tỉ lệ cấu hình không được để trống.");
        }

        // 1. Load current mappings of the product
        List<ProductEggMapping> currentMappings = mappingPersistencePort.findByKvProductId(productId);
        if (currentMappings.isEmpty()) {
            throw new BusinessRuleViolationException("Sản phẩm không có liên kết nào để cấu hình.");
        }

        // Map for easy ID lookup
        Map<String, ProductEggMapping> currentMap = currentMappings.stream()
                .collect(Collectors.toMap(ProductEggMapping::getId, m -> m));

        // 2. Validate mapping IDs and calculate sum of rates
        double sum = 0.0;
        for (UpdateMappingRateRequest req : requests) {
            if (req.getRate() < 0) {
                throw new BusinessRuleViolationException("Tỉ lệ không được âm.");
            }
            if (!currentMap.containsKey(req.getMappingId())) {
                throw new BusinessRuleViolationException("Liên kết ID " + req.getMappingId() + " không thuộc sản phẩm này.");
            }
            sum += req.getRate();
        }

        if (requests.size() != currentMappings.size()) {
            throw new BusinessRuleViolationException("Vui lòng cấu hình tỉ lệ cho toàn bộ liên kết của sản phẩm.");
        }

        // Check if sum equals 100% (within tiny tolerance)
        if (Math.abs(sum - 100.0) > 0.01) {
            throw new BusinessRuleViolationException("Tổng tỉ lệ của các liên kết phải bằng 100% (Hiện tại: " + sum + "%).");
        }

        // 3. Save new rates
        for (UpdateMappingRateRequest req : requests) {
            mappingPersistencePort.updateMappingRate(req.getMappingId(), req.getRate());
        }
    }
}
