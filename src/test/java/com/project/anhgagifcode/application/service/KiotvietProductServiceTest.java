package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.dto.*;
import com.project.anhgagifcode.application.port.out.*;
import com.project.anhgagifcode.domain.exception.BusinessRuleViolationException;
import com.project.anhgagifcode.domain.model.KiotvietProduct;
import com.project.anhgagifcode.domain.model.ProductEggMapping;
import com.project.anhgagifcode.domain.model.GiftPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KiotvietProductServiceTest {

    @Mock
    private KiotvietProductPersistencePort productPersistencePort;
    @Mock
    private ProductEggMappingPersistencePort mappingPersistencePort;
    @Mock
    private KiotvietApiPort apiPort;

    private KiotvietProduct mockProduct;
    private ProductEggMapping mockMapping;
    private GiftPool mockPool;

    @BeforeEach
    void setUp() {
        mockProduct = KiotvietProduct.builder()
                .kvProductId(100L)
                .name("Product 1")
                .fullName("Full Product 1")
                .basePrice(50000.0)
                .imageUrl("http://image")
                .lastSyncedAt(LocalDateTime.now())
                .build();

        mockPool = GiftPool.builder()
                .id("pool-1")
                .poolName("Roblox Pool")
                .tier("A")
                .build();

        mockMapping = ProductEggMapping.builder()
                .id("mapping-1")
                .eggType(1)
                .eggTier("A")
                .productCode(mockProduct)
                .giftPoolId(mockPool)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testGetAllProducts_Success() {
        GetAllProductsService service = new GetAllProductsService();
        assertThrows(UnsupportedOperationException.class, () -> service.get());
    }

    @Test
    void testGetKiotvietProducts_Success() {
        GetKiotvietProductsService service = new GetKiotvietProductsService(productPersistencePort, mappingPersistencePort);
        when(productPersistencePort.findAll()).thenReturn(List.of(mockProduct));
        when(mappingPersistencePort.findAll()).thenReturn(List.of(mockMapping));

        List<KiotvietProductDto> result = service.getProducts();

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getMappings().size());
        assertEquals("pool-1", result.get(0).getMappings().get(0).getGiftPool().getId());
    }

    @Test
    void testSyncProductsFromKiotviet_Success() {
        SyncKiotvietProductService service = new SyncKiotvietProductService(apiPort, productPersistencePort);
        when(apiPort.fetchAllProductsFromKiotviet()).thenReturn(List.of(mockProduct));
        when(productPersistencePort.saveProduct(any(KiotvietProduct.class))).thenReturn(mockProduct);

        int rows = service.syncProductsFromKiotviet();

        assertEquals(1, rows);
        verify(productPersistencePort, times(1)).saveProduct(any(KiotvietProduct.class));
    }

    @Test
    void testLinkProductToEgg_Success() {
        LinkProductToEggService service = new LinkProductToEggService(mappingPersistencePort);
        LinkProductToEggRequest request = LinkProductToEggRequest.builder()
                .productId(100L)
                .poolId("pool-1")
                .eggType(1)
                .build();

        when(mappingPersistencePort.existsByKvProductIdAndEggType(100L, 1)).thenReturn(false);
        when(mappingPersistencePort.findByKvProductId(100L)).thenReturn(Collections.emptyList());

        service.linkProductToEgg(request);

        verify(mappingPersistencePort, times(1)).saveMapping(100L, "pool-1", 1);
    }

    @Test
    void testLinkProductToEgg_LimitReached_ThrowsException() {
        LinkProductToEggService service = new LinkProductToEggService(mappingPersistencePort);
        LinkProductToEggRequest request = LinkProductToEggRequest.builder()
                .productId(100L)
                .poolId("pool-1")
                .eggType(1)
                .build();

        when(mappingPersistencePort.existsByKvProductIdAndEggType(100L, 1)).thenReturn(false);
        // Exceed max 2 eggs
        when(mappingPersistencePort.findByKvProductId(100L)).thenReturn(List.of(mockMapping, mockMapping));

        assertThrows(BusinessRuleViolationException.class, () -> service.linkProductToEgg(request));
    }

    @Test
    void testDeleteProductEggMapping_Success() {
        DeleteProductEggMappingService service = new DeleteProductEggMappingService(mappingPersistencePort);
        BatchDeleteMappingRequest request = BatchDeleteMappingRequest.builder()
                .mappingIds(List.of("mapping-1"))
                .build();

        service.deleteMappings(request);

        verify(mappingPersistencePort, times(1)).deleteMappings(List.of("mapping-1"));
    }
}
