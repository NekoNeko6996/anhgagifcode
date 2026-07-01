package com.project.anhgagifcode.application.port.in;

import com.project.anhgagifcode.application.port.in.dto.UpdateMappingRateRequest;
import java.util.List;

public interface UpdateMappingRatesUseCase {
    void updateRates(Long productId, List<UpdateMappingRateRequest> requests);
}
