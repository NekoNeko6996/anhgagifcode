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

        List<ProductEggMapping> currentMappings = mappingPersistencePort.findByKvProductId(productId);

        boolean alreadyMapped = currentMappings.stream()
                .anyMatch(m -> m.getGiftPoolId() != null 
                        && m.getGiftPoolId().getId().equals(poolId));

        if (alreadyMapped) {
            throw new BusinessRuleViolationException("Liên kết giữa sản phẩm này với bể quà chỉ định đã tồn tại.");
        }

        double newRate = 100.0 / (currentMappings.size() + 1);

        // 1. Lưu ánh xạ mới với tỉ lệ mới chia đều
        mappingPersistencePort.saveMapping(productId, poolId, newRate);

        // 2. Cập nhật lại tỉ lệ chia đều cho các ánh xạ cũ
        for (ProductEggMapping mapping : currentMappings) {
            mappingPersistencePort.updateMappingRate(mapping.getId(), newRate);
        }
    }
}
