package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.dto.UpdateMappingRateRequest;
import com.project.anhgagifcode.application.port.out.ProductEggMappingPersistencePort;
import com.project.anhgagifcode.domain.exception.BusinessRuleViolationException;
import com.project.anhgagifcode.domain.model.KiotvietProduct;
import com.project.anhgagifcode.domain.model.ProductEggMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateMappingRatesServiceTest {

    @Mock
    private ProductEggMappingPersistencePort mappingPersistencePort;

    private UpdateMappingRatesService service;
    private KiotvietProduct mockProduct;
    private ProductEggMapping mapping1;
    private ProductEggMapping mapping2;

    @BeforeEach
    void setUp() {
        service = new UpdateMappingRatesService(mappingPersistencePort);

        mockProduct = KiotvietProduct.builder()
                .kvProductId(100L)
                .build();

        mapping1 = ProductEggMapping.builder()
                .id("mapping-1")
                .productCode(mockProduct)
                .rate(50.0)
                .build();

        mapping2 = ProductEggMapping.builder()
                .id("mapping-2")
                .productCode(mockProduct)
                .rate(50.0)
                .build();
    }

    @Test
    void updateRates_Success() {
        when(mappingPersistencePort.findByKvProductId(100L)).thenReturn(List.of(mapping1, mapping2));

        List<UpdateMappingRateRequest> requests = List.of(
                new UpdateMappingRateRequest("mapping-1", 40.0),
                new UpdateMappingRateRequest("mapping-2", 60.0)
        );

        service.updateRates(100L, requests);

        verify(mappingPersistencePort, times(1)).updateMappingRate("mapping-1", 40.0);
        verify(mappingPersistencePort, times(1)).updateMappingRate("mapping-2", 60.0);
    }

    @Test
    void updateRates_SumNot100_ThrowsException() {
        when(mappingPersistencePort.findByKvProductId(100L)).thenReturn(List.of(mapping1, mapping2));

        List<UpdateMappingRateRequest> requests = List.of(
                new UpdateMappingRateRequest("mapping-1", 40.0),
                new UpdateMappingRateRequest("mapping-2", 50.0)
        );

        assertThrows(BusinessRuleViolationException.class, () -> {
            service.updateRates(100L, requests);
        });

        verify(mappingPersistencePort, never()).updateMappingRate(anyString(), anyDouble());
    }

    @Test
    void updateRates_InvalidMappingId_ThrowsException() {
        when(mappingPersistencePort.findByKvProductId(100L)).thenReturn(List.of(mapping1, mapping2));

        List<UpdateMappingRateRequest> requests = List.of(
                new UpdateMappingRateRequest("mapping-1", 50.0),
                new UpdateMappingRateRequest("mapping-invalid", 50.0)
        );

        assertThrows(BusinessRuleViolationException.class, () -> {
            service.updateRates(100L, requests);
        });

        verify(mappingPersistencePort, never()).updateMappingRate(anyString(), anyDouble());
    }
}
