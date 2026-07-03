package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.LinkProductToEggUseCase;
import com.project.anhgagifcode.application.port.in.dto.LinkProductToEggRequest;
import com.project.anhgagifcode.application.port.out.ProductEggMappingPersistencePort;
import com.project.anhgagifcode.domain.exception.BusinessRuleViolationException;
import com.project.anhgagifcode.domain.model.ProductEggMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
public class LinkProductToEggService implements LinkProductToEggUseCase {

    private final ProductEggMappingPersistencePort mappingPersistencePort;

    @Override
    @Transactional
    public void linkProductToEgg(LinkProductToEggRequest request) {
        Long productId = request.getProductId();
        String poolId = request.getPoolId();
        int mappingsType = request.getMappingsType() != null ? request.getMappingsType() : 1;

        List<ProductEggMapping> currentMappings = mappingPersistencePort.findByKvProductId(productId);

        java.util.List<ProductEggMapping> filteredMappings = currentMappings.stream()
                .filter(m -> m.getMappingsType() == mappingsType)
                .collect(java.util.stream.Collectors.toList());

        boolean alreadyMapped = filteredMappings.stream()
                .anyMatch(m -> m.getGiftPoolId() != null 
                        && m.getGiftPoolId().getId().equals(poolId));

        if (alreadyMapped) {
            throw new BusinessRuleViolationException("Liên kết giữa sản phẩm này với bể quà chỉ định thuộc nhóm này đã tồn tại.");
        }

        double newRate = 100.0 / (filteredMappings.size() + 1);

        // 1. Lưu ánh xạ mới với tỉ lệ mới chia đều
        mappingPersistencePort.saveMapping(productId, poolId, newRate, mappingsType);

        // 2. Cập nhật lại tỉ lệ chia đều cho các ánh xạ cũ cùng nhóm
        for (ProductEggMapping mapping : filteredMappings) {
            mappingPersistencePort.updateMappingRate(mapping.getId(), newRate);
        }
    }
}
