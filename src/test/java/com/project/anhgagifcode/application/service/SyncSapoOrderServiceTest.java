package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.out.EggPersistencePort;
import com.project.anhgagifcode.application.port.out.ProductEggMappingPersistencePort;
import com.project.anhgagifcode.application.port.out.SapoOrderPersistencePort;
import com.project.anhgagifcode.domain.model.Egg;
import com.project.anhgagifcode.domain.model.GiftPool;
import com.project.anhgagifcode.domain.model.ProductEggMapping;
import com.project.anhgagifcode.domain.model.SapoOrder;
import com.project.anhgagifcode.domain.model.SapoOrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncSapoOrderServiceTest {

    @Mock
    private SapoOrderPersistencePort orderPersistencePort;

    @Mock
    private EggPersistencePort eggPersistencePort;

    @Mock
    private ProductEggMappingPersistencePort mappingPersistencePort; // Đã thêm Mock mới

    @InjectMocks
    private SyncSapoOrderService syncSapoOrderService;

    private SapoOrder incomingOrder;
    private SapoOrder existingOrder;
    private ProductEggMapping mockMapping;

    @BeforeEach
    void setUp() {
        SapoOrderItem item = SapoOrderItem.builder()
                .sapoProductId("PROD1")
                .quantity(2)
                .build();

        incomingOrder = SapoOrder.builder()
                .orderCode("SON123")
                .status("open")
                .financialStatus("pending") // Set mặc định là pending cho các test cũ
                .updatedAt(LocalDateTime.of(2026, 6, 25, 10, 0))
                .orderItems(List.of(item))
                .build();

        existingOrder = SapoOrder.builder()
                .id("db-uuid-1234")
                .orderCode("SON123")
                .status("open")
                .financialStatus("pending")
                .updatedAt(LocalDateTime.of(2026, 6, 25, 9, 0)) // Cũ hơn incoming
                .build();

        // Chuẩn bị sẵn một luật ánh xạ (Mapping) giả lập
        mockMapping = ProductEggMapping.builder()
                .id("map-1")
                .sapoProductId("PROD1")
                .eggType(1)
                .eggTier("VANG")
                .giftPool(GiftPool.builder().id("pool-1").build())
                .build();
    }

    @Test
    void syncOrder_NewOrder_ShouldSaveSuccessfully() {
        when(orderPersistencePort.loadOrderByCode("SON123")).thenReturn(Optional.empty());
        SapoOrder savedOrder = SapoOrder.builder().id("new-uuid-5678").status("open").financialStatus("pending").build();
        when(orderPersistencePort.saveOrder(any(SapoOrder.class))).thenReturn(savedOrder);

        syncSapoOrderService.syncOrder(incomingOrder);

        verify(orderPersistencePort, times(1)).saveOrder(incomingOrder);
        verify(eggPersistencePort, never()).cancelEggsByOrderId(anyString());
    }

    @Test
    void syncOrder_ExistingOrder_WebhookIsOlder_ShouldIgnore() {
        incomingOrder.setUpdatedAt(LocalDateTime.of(2026, 6, 25, 8, 0));
        when(orderPersistencePort.loadOrderByCode("SON123")).thenReturn(Optional.of(existingOrder));

        syncSapoOrderService.syncOrder(incomingOrder);

        verify(orderPersistencePort, never()).saveOrder(any());
    }

    @Test
    void syncOrder_ExistingOrder_WebhookIsNewer_ShouldUpdateAndMapIds() {
        when(orderPersistencePort.loadOrderByCode("SON123")).thenReturn(Optional.of(existingOrder));
        when(orderPersistencePort.saveOrder(any(SapoOrder.class))).thenReturn(existingOrder);

        syncSapoOrderService.syncOrder(incomingOrder);

        assertEquals("db-uuid-1234", incomingOrder.getId());
        verify(orderPersistencePort, times(1)).saveOrder(incomingOrder);
    }

    @Test
    void syncOrder_OrderIsCancelled_ShouldCancelEggs() {
        incomingOrder.setStatus("cancelled");
        when(orderPersistencePort.loadOrderByCode("SON123")).thenReturn(Optional.empty());
        SapoOrder savedOrder = SapoOrder.builder().id("new-uuid-5678").status("cancelled").financialStatus("pending").build();
        when(orderPersistencePort.saveOrder(any(SapoOrder.class))).thenReturn(savedOrder);

        syncSapoOrderService.syncOrder(incomingOrder);

        verify(eggPersistencePort, times(1)).cancelEggsByOrderId("new-uuid-5678");
    }

    @Test
    void syncOrder_OrderIsPaid_ShouldGenerateEgg_WhenEligible() {
        // Arrange: Khách đã thanh toán
        incomingOrder.setFinancialStatus("paid");
        when(orderPersistencePort.loadOrderByCode("SON123")).thenReturn(Optional.empty());

        SapoOrder savedOrder = SapoOrder.builder()
                .id("new-uuid-5678")
                .status("open")
                .financialStatus("paid")
                .orderCode("SON123")
                .orderItems(incomingOrder.getOrderItems())
                .build();
                
        when(orderPersistencePort.saveOrder(any(SapoOrder.class))).thenReturn(savedOrder);
        
        // Giả lập tìm thấy luật (mua PROD1 thì tặng Trứng loại 1)
        when(mappingPersistencePort.loadHighestTierMapping(List.of("PROD1"))).thenReturn(Optional.of(mockMapping));
        // Giả lập đơn này chưa từng được nhận trứng loại 1
        when(eggPersistencePort.existsByOrderIdAndEggType("new-uuid-5678", 1)).thenReturn(false);

        // Act
        syncSapoOrderService.syncOrder(incomingOrder);

        // Assert: Phải gọi lệnh lưu 1 quả trứng mới
        verify(eggPersistencePort, times(1)).saveEgg(any(Egg.class));
    }

    @Test
    void syncOrder_OrderIsPaid_ShouldNotGenerateEgg_WhenNotEligible() {
        // Arrange
        incomingOrder.setFinancialStatus("paid");
        when(orderPersistencePort.loadOrderByCode("SON123")).thenReturn(Optional.empty());

        SapoOrder savedOrder = SapoOrder.builder()
                .id("new-uuid-5678")
                .status("open")
                .financialStatus("paid")
                .orderItems(incomingOrder.getOrderItems())
                .build();
                
        when(orderPersistencePort.saveOrder(any(SapoOrder.class))).thenReturn(savedOrder);
        
        // Giả lập tìm KHÔNG thấy luật nào cho PROD1 (sản phẩm không có quà)
        when(mappingPersistencePort.loadHighestTierMapping(List.of("PROD1"))).thenReturn(Optional.empty());

        // Act
        syncSapoOrderService.syncOrder(incomingOrder);

        // Assert: KHÔNG được gọi lệnh lưu trứng
        verify(eggPersistencePort, never()).saveEgg(any(Egg.class));
    }

    @Test
    void syncOrder_OrderIsPaid_ShouldNotGenerateEgg_WhenEggAlreadyExists() {
        // Arrange
        incomingOrder.setFinancialStatus("paid");
        when(orderPersistencePort.loadOrderByCode("SON123")).thenReturn(Optional.empty());

        SapoOrder savedOrder = SapoOrder.builder()
                .id("new-uuid-5678")
                .status("open")
                .financialStatus("paid")
                .orderItems(incomingOrder.getOrderItems())
                .build();
                
        when(orderPersistencePort.saveOrder(any(SapoOrder.class))).thenReturn(savedOrder);
        when(mappingPersistencePort.loadHighestTierMapping(List.of("PROD1"))).thenReturn(Optional.of(mockMapping));
        
        // Giả lập đơn này ĐÃ TỪNG được nhận trứng loại 1 rồi (do webhook gửi trùng nhiều lần)
        when(eggPersistencePort.existsByOrderIdAndEggType("new-uuid-5678", 1)).thenReturn(true);

        // Act
        syncSapoOrderService.syncOrder(incomingOrder);

        // Assert: KHÔNG được sinh thêm trứng để tránh lặp quà
        verify(eggPersistencePort, never()).saveEgg(any(Egg.class));
    }
}