package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.DeleteProductEggMappingUseCase;
import com.project.anhgagifcode.application.port.in.dto.BatchDeleteMappingRequest;
import com.project.anhgagifcode.application.port.out.ProductEggMappingPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class DeleteProductEggMappingService implements DeleteProductEggMappingUseCase {

    private final ProductEggMappingPersistencePort mappingPersistencePort;

    @Override
    @Transactional
    public void deleteMapping(String mappingId) {
        mappingPersistencePort.deleteMapping(mappingId);
    }

    @Override
    @Transactional
    public void deleteMappings(BatchDeleteMappingRequest request) {
        mappingPersistencePort.deleteMappings(request.getMappingIds());
    }
}
