package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.out.CustomerPersistencePort;
import com.project.anhgagifcode.application.port.out.EggPersistencePort;
import com.project.anhgagifcode.domain.exception.BusinessRuleViolationException;
import com.project.anhgagifcode.domain.exception.ResourceNotFoundException;
import com.project.anhgagifcode.domain.model.Customer;
import com.project.anhgagifcode.domain.model.Egg;
import com.project.anhgagifcode.domain.model.KiotvietOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApproveEarlyHatchServiceTest {

    @Mock
    private EggPersistencePort eggPort;
    @Mock
    private CustomerPersistencePort customerPort;
    @Mock
    private PlatformTransactionManager transactionManager;

    private ApproveEarlyHatchService service;
    private Egg hatchingEgg;
    private Customer eligibleCustomer;

    @BeforeEach
    void setUp() {
        service = new ApproveEarlyHatchService(eggPort, customerPort, transactionManager);
        
        KiotvietOrder order = KiotvietOrder.builder()
                .id("order-1")
                .customerCode("CUS001")
                .build();

        hatchingEgg = Egg.builder()
                .id("egg-1")
                .eggType(2)
                .status("HATCHING")
                .hatchAt(LocalDateTime.now().plusDays(10))
                .order(order)
                .build();

        eligibleCustomer = Customer.builder()
                .id("cus-1")
                .customerCode("CUS001")
                .earlyHatchCredits(2)
                .build();

        lenient().when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
    }

    @Test
    void approveEarlyHatch_Success_DeductsCreditAndReducesHatchTime() {
        when(eggPort.loadEggForUpdate("egg-1")).thenReturn(Optional.of(hatchingEgg));
        when(customerPort.loadByCustomerCodeForUpdate("CUS001")).thenReturn(Optional.of(eligibleCustomer));

        LocalDateTime originalHatchAt = hatchingEgg.getHatchAt();

        service.approveEarlyHatch("egg-1");

        assertEquals(1, eligibleCustomer.getEarlyHatchCredits());
        assertEquals(originalHatchAt.minusDays(3), hatchingEgg.getHatchAt());
        assertEquals("HATCHING", hatchingEgg.getStatus());

        verify(customerPort, times(1)).saveCustomer(eligibleCustomer);
        verify(eggPort, times(1)).saveEgg(hatchingEgg);
    }

    @Test
    void approveEarlyHatch_Success_TriggersReadyToClaim() {
        // Only 2 days left
        hatchingEgg.setHatchAt(LocalDateTime.now().plusDays(2));
        when(eggPort.loadEggForUpdate("egg-1")).thenReturn(Optional.of(hatchingEgg));
        when(customerPort.loadByCustomerCodeForUpdate("CUS001")).thenReturn(Optional.of(eligibleCustomer));

        service.approveEarlyHatch("egg-1");

        assertEquals(1, eligibleCustomer.getEarlyHatchCredits());
        assertEquals("READY_TO_CLAIM", hatchingEgg.getStatus());

        verify(customerPort, times(1)).saveCustomer(eligibleCustomer);
        verify(eggPort, times(1)).saveEgg(hatchingEgg);
    }

    @Test
    void approveEarlyHatch_NoCredits_ThrowsException() {
        eligibleCustomer.setEarlyHatchCredits(0);
        when(eggPort.loadEggForUpdate("egg-1")).thenReturn(Optional.of(hatchingEgg));
        when(customerPort.loadByCustomerCodeForUpdate("CUS001")).thenReturn(Optional.of(eligibleCustomer));

        BusinessRuleViolationException exception = assertThrows(BusinessRuleViolationException.class, () -> {
            service.approveEarlyHatch("egg-1");
        });

        assertTrue(exception.getMessage().contains("không đủ lượt"));
        verify(customerPort, never()).saveCustomer(any());
        verify(eggPort, never()).saveEgg(any());
    }

    @Test
    void approveEarlyHatch_EggNotHatching_ThrowsException() {
        hatchingEgg.setHatchAt(null);
        when(eggPort.loadEggForUpdate("egg-1")).thenReturn(Optional.of(hatchingEgg));

        BusinessRuleViolationException exception = assertThrows(BusinessRuleViolationException.class, () -> {
            service.approveEarlyHatch("egg-1");
        });

        assertTrue(exception.getMessage().contains("đã nở hoặc không ở trạng thái ấp"));
    }

    @Test
    void approveEarlyHatch_EggNotFound_ThrowsException() {
        when(eggPort.loadEggForUpdate("egg-unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            service.approveEarlyHatch("egg-unknown");
        });
    }
}
