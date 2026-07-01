package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.DeleteProductEggMappingUseCase;
import com.project.anhgagifcode.application.port.in.dto.BatchDeleteMappingRequest;
import com.project.anhgagifcode.application.port.out.ProductEggMappingPersistencePort;
import com.project.anhgagifcode.domain.model.ProductEggMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
public class DeleteProductEggMappingService implements DeleteProductEggMappingUseCase {

    private final ProductEggMappingPersistencePort mappingPersistencePort;

    @Override
    @Transactional
    public void deleteMapping(String mappingId) {
        ProductEggMapping mapping = mappingPersistencePort.findById(mappingId)
                .orElseThrow(() -> new com.project.anhgagifcode.domain.exception.ResourceNotFoundException("Không tìm thấy liên kết này."));
        
        Long productId = mapping.getProductCode().getKvProductId();

        mappingPersistencePort.deleteMapping(mappingId);

        recalculateRates(productId);
    }

    @Override
    @Transactional
    public void deleteMappings(BatchDeleteMappingRequest request) {
        if (request.getMappingIds() == null || request.getMappingIds().isEmpty()) {
            return;
        }

        java.util.Set<Long> productIdsToRecalculate = new java.util.HashSet<>();
        for (String id : request.getMappingIds()) {
            mappingPersistencePort.findById(id).ifPresent(m -> {
                if (m.getProductCode() != null) {
                    productIdsToRecalculate.add(m.getProductCode().getKvProductId());
                }
            });
        }

        mappingPersistencePort.deleteMappings(request.getMappingIds());

        for (Long productId : productIdsToRecalculate) {
            recalculateRates(productId);
        }
    }

    private void recalculateRates(Long productId) {
        List<ProductEggMapping> remaining = mappingPersistencePort.findByKvProductId(productId);
        if (!remaining.isEmpty()) {
            double equalRate = 100.0 / remaining.size();
            for (ProductEggMapping r : remaining) {
                mappingPersistencePort.updateMappingRate(r.getId(), equalRate);
            }
        }
    }
}
