package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.dto.*;
import com.project.anhgagifcode.application.port.out.CustomerPersistencePort;
import com.project.anhgagifcode.application.port.out.EggPersistencePort;
import com.project.anhgagifcode.application.port.out.KiotvietOrderPersistencePort;
import com.project.anhgagifcode.domain.exception.ResourceNotFoundException;
import com.project.anhgagifcode.domain.model.Customer;
import com.project.anhgagifcode.domain.model.Egg;
import com.project.anhgagifcode.domain.model.KiotvietOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerAndOrderServiceTest {

    @Mock
    private CustomerPersistencePort customerPort;
    @Mock
    private KiotvietOrderPersistencePort orderPort;
    @Mock
    private EggPersistencePort eggPort;

    private Customer mockCustomer;
    private KiotvietOrder mockOrder;
    private Egg mockEgg;

    @BeforeEach
    void setUp() {
        mockCustomer = Customer.builder()
                .id("cus-1")
                .customerCode("CUS001")
                .customerName("John Doe")
                .status("NEW")
                .successCount(0)
                .returnStreak(0)
                .warningCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        mockOrder = KiotvietOrder.builder()
                .id("order-1")
                .orderCode("OD001")
                .customerCode("CUS001")
                .deliveryStatus("Đã giao hàng")
                .createdAt(LocalDateTime.now())
                .build();

        mockEgg = Egg.builder()
                .id("egg-1")
                .eggType(1)
                .status("READY_TO_CLAIM")
                .createdAt(LocalDateTime.now())
                .order(mockOrder)
                .build();
    }

    @Test
    void testGetCustomers_Success() {
        GetCustomersService service = new GetCustomersService(customerPort);
        when(customerPort.findAll()).thenReturn(List.of(mockCustomer));

        List<CustomerDto> result = service.getCustomers();

        assertEquals(1, result.size());
        assertEquals("CUS001", result.get(0).getCustomerCode());
    }

    @Test
    void testUpdateCustomerStatus_Success() {
        UpdateCustomerStatusService service = new UpdateCustomerStatusService(customerPort);
        UpdateCustomerStatusRequest request = UpdateCustomerStatusRequest.builder()
                .status("WARNING")
                .returnStreak(1)
                .successCount(5)
                .build();

        when(customerPort.loadByCustomerCode("CUS001")).thenReturn(Optional.of(mockCustomer));
        when(customerPort.saveCustomer(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerDto updated = service.updateCustomerStatus("CUS001", request);

        assertNotNull(updated);
        assertEquals("WARNING", updated.getStatus());
        assertEquals(1, updated.getReturnStreak());
        assertEquals(5, updated.getSuccessCount());
    }

    @Test
    void testUpdateCustomerStatus_NotFound_ThrowsException() {
        UpdateCustomerStatusService service = new UpdateCustomerStatusService(customerPort);
        UpdateCustomerStatusRequest request = UpdateCustomerStatusRequest.builder()
                .status("WARNING")
                .build();

        when(customerPort.loadByCustomerCode("CUS_UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.updateCustomerStatus("CUS_UNKNOWN", request));
    }

    @Test
    void testGetEggs_Success() {
        GetEggsService service = new GetEggsService(eggPort);
        when(eggPort.findAll()).thenReturn(List.of(mockEgg));

        List<EggDto> eggs = service.getEggs();

        assertEquals(1, eggs.size());
        assertEquals("egg-1", eggs.get(0).getId());
        assertEquals("OD001", eggs.get(0).getOrder().getOrderCode());
    }

    @Test
    void testGetKiotvietOrders_Success() {
        GetKiotvietOrdersService service = new GetKiotvietOrdersService(orderPort);
        when(orderPort.findAll()).thenReturn(List.of(mockOrder));

        List<KiotvietOrderDto> orders = service.getOrders();

        assertEquals(1, orders.size());
        assertEquals("OD001", orders.get(0).getOrderCode());
    }
}
