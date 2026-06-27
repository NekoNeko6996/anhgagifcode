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
        int eggType = request.getEggType();

        // 1. check if the product has already been mapped to this eggType
        boolean alreadyMapped = mappingPersistencePort.existsByKvProductIdAndEggType(productId, eggType);

        // 2. check how many mapping rules currently exist for this product
        List<ProductEggMapping> currentMappings = mappingPersistencePort.findByKvProductId(productId);

        if (!alreadyMapped && currentMappings.size() >= 2) {
            throw new BusinessRuleViolationException("Sản phẩm này đã đạt số lượng liên kết tối đa (tối đa 2 trứng).");
        }

        mappingPersistencePort.saveMapping(productId, request.getPoolId(), eggType);
    }
}
