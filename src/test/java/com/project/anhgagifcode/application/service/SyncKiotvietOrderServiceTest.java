package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.dto.SyncOrderResponse;
import com.project.anhgagifcode.application.port.out.*;
import com.project.anhgagifcode.domain.exception.BusinessRuleViolationException;
import com.project.anhgagifcode.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncKiotvietOrderServiceTest {

    @Mock
    private KiotvietOrderPersistencePort orderPort;
    @Mock
    private KiotvietApiPort apiPort;
    @Mock
    private CustomerPersistencePort customerPort;
    @Mock
    private ProductEggMappingPersistencePort mappingPort;
    @Mock
    private EggPersistencePort eggPort;

    @InjectMocks
    private SyncKiotvietOrderService syncService;

    private KiotvietOrder apiOrder;
    private Customer customer;
    private ProductEggMapping mapping;

    @BeforeEach
    void setUp() {
        KiotvietOrderItem item = KiotvietOrderItem.builder().kvProductId("PROD-1").quantity(1).build();
        
        apiOrder = KiotvietOrder.builder()
                .id("order-1")
                .orderCode("KV-123")
                .customerCode("CUS-123")
                .deliveryStatus("Giao thành công")
                .orderItems(List.of(item))
                .build();

        customer = Customer.builder()
                .id("cus-uuid")
                .customerCode("CUS-123")
                .customerName("Nguyen Van A")
                .status("NEW")
                .successCount(0)
                .returnStreak(0)
                .warningCount(0)
                .build();

        mapping = ProductEggMapping.builder()
                .kvProductId("PROD-1")
                .eggType(1)
                .giftPool(GiftPool.builder().id("pool-1").build())
                .build();
    }

    @Test
    void syncAndGetOrderDetails_NewOrder_Success() {
        // Mock API trả về đơn hàng
        when(orderPort.loadByOrderCode("KV-123")).thenReturn(Optional.empty());
        when(apiPort.fetchOrderFromKiotviet("KV-123")).thenReturn(Optional.of(apiOrder));
        
        // Mock Customer
        when(customerPort.loadByCustomerCode("CUS-123")).thenReturn(Optional.of(customer));
        when(customerPort.saveCustomer(any(Customer.class))).thenReturn(customer);
        
        // Mock lưu Order
        when(orderPort.saveOrder(any(KiotvietOrder.class))).thenReturn(apiOrder);
        
        // Mock luật sinh trứng
        when(mappingPort.loadMappingsByProductIds(List.of("PROD-1"))).thenReturn(List.of(mapping));
        when(eggPort.existsByOrderIdAndPoolIdAndEggType(anyString(), anyString(), anyInt())).thenReturn(false);
        when(eggPort.loadEggsByOrderId(anyString())).thenReturn(List.of(
                Egg.builder().id("egg-1").eggType(1).status("PENDING").build()
        ));

        SyncOrderResponse response = syncService.syncAndGetOrderDetails("KV-123");

        assertNotNull(response);
        assertEquals("Nguyen Van A", response.getCustomerName());
        assertEquals(1, response.getEggs().size());
        assertEquals("READY_TO_CLAIM", response.getEggs().get(0).getDisplayStatus());
        
        verify(customerPort, times(1)).saveCustomer(any(Customer.class));
        verify(eggPort, times(1)).saveEgg(any(Egg.class));
    }

    @Test
    void syncAndGetOrderDetails_CustomerIsBanned_ThrowsException() {
        customer.setStatus("BANNED");
        
        when(orderPort.loadByOrderCode("KV-123")).thenReturn(Optional.empty());
        when(apiPort.fetchOrderFromKiotviet("KV-123")).thenReturn(Optional.of(apiOrder));
        when(customerPort.loadByCustomerCode("CUS-123")).thenReturn(Optional.of(customer));
        when(customerPort.saveCustomer(any(Customer.class))).thenReturn(customer);
        when(orderPort.saveOrder(any())).thenReturn(apiOrder);

        BusinessRuleViolationException exception = assertThrows(BusinessRuleViolationException.class, () -> {
            syncService.syncAndGetOrderDetails("KV-123");
        });

        assertTrue(exception.getMessage().contains("bị cấm"));
    }

    @Test
    void syncAndGetOrderDetails_WithinCooldown_DoesNotCallApi() {
        apiOrder.setLastSyncedAt(LocalDateTime.now().minusMinutes(5)); // Mới sync 5 phút trước
        
        when(orderPort.loadByOrderCode("KV-123")).thenReturn(Optional.of(apiOrder));
        when(customerPort.loadByCustomerCode("CUS-123")).thenReturn(Optional.of(customer));
        when(eggPort.loadEggsByOrderId(anyString())).thenReturn(List.of());

        syncService.syncAndGetOrderDetails("KV-123");

        verify(apiPort, never()).fetchOrderFromKiotviet(anyString());
        verify(orderPort, never()).saveOrder(any());
    }
}