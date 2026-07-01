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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    @Mock
    private org.springframework.transaction.PlatformTransactionManager transactionManager;

    @InjectMocks
    private SyncKiotvietOrderService syncService;

    private KiotvietOrder mockOrder;
    private Customer cleanCustomer;
    private Customer warningCustomer;
    private Customer bannedCustomer;
    private ProductEggMapping mappingEgg1Lowest;
    private ProductEggMapping mappingEgg1Highest;
    private ProductEggMapping mappingEgg2Lowest;

    @BeforeEach
    void setUp() {
        mockOrder = KiotvietOrder.builder()
                .id("order-id")
                .orderCode("OD123")
                .customerCode("CUS99")
                .deliveryStatus("Đã giao hàng")
                .lastSyncedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .orderItems(List.of(
                        KiotvietOrderItem.builder()
                                .id("item-1")
                                .kvProductId("prod-1")
                                .quantity(1)
                                .build()
                ))
                .build();

        cleanCustomer = Customer.builder()
                .id("cus-clean-id")
                .customerCode("CUS99")
                .status("NEW")
                .successCount(0)
                .returnStreak(0)
                .build();

        warningCustomer = Customer.builder()
                .id("cus-warning-id")
                .customerCode("CUS99")
                .status("WARNING")
                .successCount(1)
                .returnStreak(1)
                .build();

        bannedCustomer = Customer.builder()
                .id("cus-banned-id")
                .customerCode("CUS99")
                .status("BANNED")
                .successCount(0)
                .returnStreak(2)
                .build();

        GiftPool poolA = GiftPool.builder().id("pool-a").tier("A").build();
        GiftPool poolB = GiftPool.builder().id("pool-b").tier("B").build();

        mappingEgg1Lowest = ProductEggMapping.builder()
                .id("mapping-1")
                .giftPoolId(poolA)
                .rate(100.0)
                .build();

        mappingEgg1Highest = ProductEggMapping.builder()
                .id("mapping-2")
                .giftPoolId(poolB)
                .rate(100.0)
                .build();

        mappingEgg2Lowest = ProductEggMapping.builder()
                .id("mapping-3")
                .giftPoolId(poolA)
                .rate(100.0)
                .build();

        lenient().when(transactionManager.getTransaction(any())).thenReturn(mock(org.springframework.transaction.TransactionStatus.class));
    }

    @Test
    void syncAndGetOrderDetails_GuestCustomer_FirstTimeSync_ThrowsException() {
        KiotvietOrder guestOrder = KiotvietOrder.builder()
                .id("order-guest")
                .orderCode("OD-GUEST")
                .customerCode("KHACH_LE")
                .deliveryStatus("Đang chuẩn bị hàng")
                .build();
        when(orderPort.loadByOrderCode("OD-GUEST")).thenReturn(Optional.empty());
        when(apiPort.fetchOrderFromKiotviet("OD-GUEST")).thenReturn(Optional.of(guestOrder));

        BusinessRuleViolationException exception = assertThrows(BusinessRuleViolationException.class, () -> {
            syncService.syncAndGetOrderDetails("OD-GUEST");
        });

        assertEquals("Thiếu thông tin khách hàng", exception.getMessage());
    }

    @Test
    void syncAndGetOrderDetails_GuestCustomer_ExistingOrder_ThrowsException() {
        KiotvietOrder guestOrder = KiotvietOrder.builder()
                .id("order-guest")
                .orderCode("OD-GUEST")
                .customerCode("")
                .deliveryStatus("Đang chuẩn bị hàng")
                .build();
        when(orderPort.loadByOrderCode("OD-GUEST")).thenReturn(Optional.of(guestOrder));

        BusinessRuleViolationException exception = assertThrows(BusinessRuleViolationException.class, () -> {
            syncService.syncAndGetOrderDetails("OD-GUEST");
        });

        assertEquals("Thiếu thông tin khách hàng", exception.getMessage());
    }

    @Test
    void syncAndGetOrderDetails_OrderNotDelivered_NoEggsGenerated() {
        mockOrder.setDeliveryStatus("Đang chuẩn bị hàng");
        when(orderPort.loadByOrderCode("OD123")).thenReturn(Optional.of(mockOrder));
        when(customerPort.loadByCustomerCode("CUS99")).thenReturn(Optional.of(cleanCustomer));

        syncService.syncAndGetOrderDetails("OD123");

        verify(eggPort, never()).saveEgg(any(Egg.class));
    }

    @Test
    void syncAndGetOrderDetails_BannedCustomer_ThrowsException() {
        when(orderPort.loadByOrderCode("OD123")).thenReturn(Optional.of(mockOrder));
        when(customerPort.loadByCustomerCode("CUS99")).thenReturn(Optional.of(bannedCustomer));

        BusinessRuleViolationException exception = assertThrows(BusinessRuleViolationException.class, () -> {
            syncService.syncAndGetOrderDetails("OD123");
        });

        assertEquals("Tài khoản bị khóa do vi phạm chính sách", exception.getMessage());
    }

    @Test
    void syncAndGetOrderDetails_CleanCustomer_Egg1NoCooldown() {
        when(orderPort.loadByOrderCode("OD123")).thenReturn(Optional.of(mockOrder));
        when(customerPort.loadByCustomerCode("CUS99")).thenReturn(Optional.of(cleanCustomer));
        
        // Product mappings return Egg 1 & Egg 2
        when(mappingPort.loadMappingsByProductIds(List.of("prod-1")))
                .thenReturn(List.of(mappingEgg1Lowest, mappingEgg2Lowest));

        when(eggPort.loadEggsByOrderId("order-id")).thenReturn(Collections.emptyList());

        // Stub eggPort.saveEgg to capture generated eggs
        List<Egg> generatedEggs = new ArrayList<>();
        doAnswer(inv -> {
            generatedEggs.add(inv.getArgument(0));
            return null;
        }).when(eggPort).saveEgg(any(Egg.class));

        // Calling sync service
        syncService.syncAndGetOrderDetails("OD123");

        assertEquals(2, generatedEggs.size());
        
        Egg egg1 = generatedEggs.stream().filter(e -> e.getEggType() == 1).findFirst().orElseThrow();
        Egg egg2 = generatedEggs.stream().filter(e -> e.getEggType() == 2).findFirst().orElseThrow();

        // Egg 1 of clean customer has NO hatch cooldown
        assertNull(egg1.getHatchAt());
        // Egg 2 of clean customer HAS 15-day hatching cooldown
        assertNotNull(egg2.getHatchAt());
        assertTrue(egg2.getHatchAt().isAfter(LocalDateTime.now().plusDays(14)));
    }

    @Test
    void syncAndGetOrderDetails_WarningCustomer_Egg1Gets15DaysCooldown() {
        when(orderPort.loadByOrderCode("OD123")).thenReturn(Optional.of(mockOrder));
        when(customerPort.loadByCustomerCode("CUS99")).thenReturn(Optional.of(warningCustomer));

        when(mappingPort.loadMappingsByProductIds(List.of("prod-1")))
                .thenReturn(List.of(mappingEgg1Lowest, mappingEgg2Lowest));

        when(eggPort.loadEggsByOrderId("order-id")).thenReturn(Collections.emptyList());

        List<Egg> generatedEggs = new ArrayList<>();
        doAnswer(inv -> {
            generatedEggs.add(inv.getArgument(0));
            return null;
        }).when(eggPort).saveEgg(any(Egg.class));

        syncService.syncAndGetOrderDetails("OD123");

        assertEquals(2, generatedEggs.size());
        Egg egg1 = generatedEggs.stream().filter(e -> e.getEggType() == 1).findFirst().orElseThrow();
        Egg egg2 = generatedEggs.stream().filter(e -> e.getEggType() == 2).findFirst().orElseThrow();

        // Warning customer: Both eggs get 15 days cooldown
        assertNotNull(egg1.getHatchAt());
        assertTrue(egg1.getHatchAt().isAfter(LocalDateTime.now().plusDays(14)));
        assertNotNull(egg2.getHatchAt());
        assertTrue(egg2.getHatchAt().isAfter(LocalDateTime.now().plusDays(14)));
    }

    @Test
    void syncAndGetOrderDetails_CacheCooldown_NoApiCall() {
        // order has lastSyncedAt = now (cache is valid since <= 5 minutes)
        mockOrder.setLastSyncedAt(LocalDateTime.now());
        when(orderPort.loadByOrderCode("OD123")).thenReturn(Optional.of(mockOrder));
        when(customerPort.loadByCustomerCode("CUS99")).thenReturn(Optional.of(cleanCustomer));

        syncService.syncAndGetOrderDetails("OD123");

        // Verify API was NEVER called
        verify(apiPort, never()).fetchOrderFromKiotviet(anyString());
    }

    @Test
    void syncAndGetOrderDetails_CacheExpired_ApiCallExecuted() {
        // order lastSyncedAt = 10 mins ago (cache expired > 5 minutes)
        mockOrder.setLastSyncedAt(LocalDateTime.now().minusMinutes(10));
        when(orderPort.loadByOrderCode("OD123")).thenReturn(Optional.of(mockOrder));
        when(customerPort.loadByCustomerCode("CUS99")).thenReturn(Optional.of(cleanCustomer));

        // Mock API response
        KiotvietOrder apiOrder = KiotvietOrder.builder()
                .id("order-id")
                .orderCode("OD123")
                .customerCode("CUS99")
                .deliveryStatus("Đang giao hàng")
                .orderItems(mockOrder.getOrderItems())
                .build();
        when(apiPort.fetchOrderFromKiotviet("OD123")).thenReturn(Optional.of(apiOrder));
        when(orderPort.saveOrder(any(KiotvietOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        syncService.syncAndGetOrderDetails("OD123");

        // Verify API WAS called
        verify(apiPort, times(1)).fetchOrderFromKiotviet("OD123");
    }

    @Test
    void syncAndGetOrderDetails_SuffixInput_FirstTimeSync_TriesPrefixesAndSucceeds() {
        String suffix = "260628ABEF6676";
        String expectedFullCode = "HDSPE_260628ABEF6676";
        
        KiotvietOrder expectedOrder = KiotvietOrder.builder()
                .id("order-id")
                .orderCode(expectedFullCode)
                .customerCode("CUS99")
                .deliveryStatus("Đã giao hàng")
                .orderItems(List.of(
                        KiotvietOrderItem.builder()
                                .id("item-1")
                                .kvProductId("prod-1")
                                .quantity(1)
                                .build()
                ))
                .build();
                
        // Mock orderPort.loadByOrderCode to return empty for suffix
        when(orderPort.loadByOrderCode(suffix)).thenReturn(Optional.empty());
        
        // Mock findDistinctPrefixes to return some DB prefixes
        when(orderPort.findDistinctPrefixes()).thenReturn(List.of("MY_CUSTOM_PREFIX"));
        
        // Mock apiPort.fetchOrderFromKiotviet
        lenient().when(apiPort.fetchOrderFromKiotviet(suffix)).thenReturn(Optional.empty());
        lenient().when(apiPort.fetchOrderFromKiotviet(anyString())).thenReturn(Optional.empty());
        lenient().when(apiPort.fetchOrderFromKiotviet(expectedFullCode)).thenReturn(Optional.of(expectedOrder));
        
        // Mock persistence
        when(orderPort.saveOrder(any(KiotvietOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        when(customerPort.loadByCustomerCode("CUS99")).thenReturn(Optional.of(cleanCustomer));
        when(customerPort.saveCustomer(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        
        // Call service
        SyncOrderResponse response = syncService.syncAndGetOrderDetails(suffix);
        
        // Verify
        assertNotNull(response);
        assertEquals("NEW", response.getCustomerStatus());
        assertEquals("Đã giao hàng", response.getDeliveryStatus());
        
        // Verify apiPort was called with expected Full Code
        verify(apiPort, atLeastOnce()).fetchOrderFromKiotviet(expectedFullCode);
    }
}
