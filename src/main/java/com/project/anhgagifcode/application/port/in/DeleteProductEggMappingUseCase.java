package com.project.anhgagifcode.application.port.in;

import com.project.anhgagifcode.application.port.in.dto.BatchDeleteMappingRequest;

public interface DeleteProductEggMappingUseCase {
    void deleteMapping(String mappingId);
    void deleteMappings(BatchDeleteMappingRequest request);
}
