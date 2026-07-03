package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.SyncKiotvietOrderUseCase;
import com.project.anhgagifcode.application.port.in.dto.ClaimEggResponse;
import com.project.anhgagifcode.application.port.out.*;
import com.project.anhgagifcode.domain.exception.BusinessRuleViolationException;
import com.project.anhgagifcode.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimEggServiceTest {

    @Mock
    private EggPersistencePort eggPort;
    @Mock
    private GiftAccountPersistencePort accountPort;
    @Mock
    private EggOpeningLogPersistencePort logPort;
    @Mock
    private KiotvietOrderPersistencePort orderPort;
    @Mock
    private CustomerPersistencePort customerPort;
    @Mock
    private SyncKiotvietOrderUseCase syncOrderUseCase;
    @Mock
    private NotificationPort notificationPort;
    @Mock
    private PlatformTransactionManager transactionManager;

    @InjectMocks
    private ClaimEggService claimService;

    private Egg validEgg;
    private KiotvietOrder validOrder;
    private Customer cleanCustomer;
    private Customer warningCustomer;
    private Customer bannedCustomer;
    private GiftAccount availableAccount;

    @BeforeEach
    void setUp() {
        validOrder = KiotvietOrder.builder()
                .id("order-uuid")
                .orderCode("OD100")
                .customerCode("CUS88")
                .deliveryStatus("Đã giao hàng")
                .lastSyncedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now().minusDays(20))
                .build();

        validEgg = Egg.builder()
                .id("egg-uuid")
                .eggType(1)
                .status("READY_TO_CLAIM")
                .giftPool(GiftPool.builder().id("pool-uuid").tier("A").build())
                .order(validOrder)
                .productCode("prod-1") // Vẫn giữ ở model để test dữ liệu, nhưng không dùng để query nữa
                .build();

        cleanCustomer = Customer.builder()
                .id("cus-clean-uuid")
                .customerCode("CUS88")
                .status("NEW")
                .successCount(0)
                .returnStreak(0)
                .build();

        warningCustomer = Customer.builder()
                .id("cus-warning-uuid")
                .customerCode("CUS88")
                .status("WARNING")
                .successCount(2)
                .returnStreak(1)
                .build();

        bannedCustomer = Customer.builder()
                .id("cus-banned-uuid")
                .customerCode("CUS88")
                .status("BANNED")
                .successCount(0)
                .returnStreak(2)
                .build();

        availableAccount = GiftAccount.builder()
                .id("acc-uuid")
                .username("gift_user")
                .password("gift_pass")
                .platform("Steam")
                .status("AVAILABLE")
                .build();

        lenient().when(syncOrderUseCase.syncOrderIfNeeded(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
        
        // Cập nhật lại mock: Bỏ tham số productCode (anyString())
        lenient().when(eggPort.loadEggsForClaimReadOnly(anyString(), anyInt()))
                 .thenAnswer(invocation -> {
                     String orderId = invocation.getArgument(0);
                     int type = invocation.getArgument(1);
                     return eggPort.loadEggsForClaim(orderId, type); 
                 });
    }

    @Test
    void claimEggReward_Success_CleanCustomer_Egg1_Immediate() {
        when(eggPort.loadEggsForClaim("order-uuid", 1)).thenReturn(List.of(validEgg));
        when(customerPort.loadByCustomerCodeForUpdate("CUS88")).thenReturn(Optional.of(cleanCustomer));
        when(accountPort.pickAvailableAccountForUpdateSkipLocked("pool-uuid")).thenReturn(Optional.of(availableAccount));
        when(eggPort.loadEggsByOrderId("order-uuid")).thenReturn(List.of(validEgg));

        ClaimEggResponse response = claimService.claimEggReward("order-uuid", 1, "127.0.0.1");

        assertNotNull(response);
        assertEquals(1, response.getAccounts().size());
        assertEquals("gift_user", response.getAccounts().get(0).getUsername());
        assertEquals("Steam", response.getAccounts().get(0).getPlatform());
        assertEquals("A", response.getAccounts().get(0).getTier());

        verify(accountPort, times(1)).updateAccount(argThat(acc -> "ASSIGNED".equals(acc.getStatus())));
        verify(eggPort, times(1)).saveAllEggs(anyList());
        verify(logPort, times(1)).saveLog(any(EggOpeningLog.class));
    }

    @Test
    void claimEggReward_BannedCustomer_ThrowsException() {
        when(eggPort.loadEggsForClaim("order-uuid", 1)).thenReturn(List.of(validEgg));
        when(customerPort.loadByCustomerCodeForUpdate("CUS88")).thenReturn(Optional.of(bannedCustomer));

        BusinessRuleViolationException exception = assertThrows(BusinessRuleViolationException.class, () -> {
            claimService.claimEggReward("order-uuid", 1, "127.0.0.1");
        });

        assertEquals("Tài khoản bị khóa do vi phạm chính sách", exception.getMessage());
    }

    @Test
    void claimEggReward_HatchingCooldown_ThrowsException() {
        validEgg.setEggType(2);
        validEgg.setStatus("HATCHING");
        validEgg.setHatchAt(LocalDateTime.now().plusDays(2)); // Hatch date in future
        when(eggPort.loadEggsForClaim("order-uuid", 2)).thenReturn(List.of(validEgg));
        when(customerPort.loadByCustomerCodeForUpdate("CUS88")).thenReturn(Optional.of(cleanCustomer));

        BusinessRuleViolationException exception = assertThrows(BusinessRuleViolationException.class, () -> {
            claimService.claimEggReward("order-uuid", 2, "127.0.0.1");
        });

        assertTrue(exception.getMessage().contains("chưa đến thời gian"));
    }

    @Test
    void claimEggReward_Egg2_CooldownFinished_Succeeds() {
        validEgg.setEggType(2);
        validEgg.setStatus("WAITING_ORDER_COMPLETION");
        validEgg.setHatchAt(LocalDateTime.now().minusMinutes(5)); // Cooldown finished
        validOrder.setCreatedAt(LocalDateTime.now()); // Make it recent
        when(eggPort.loadEggsForClaim("order-uuid", 2)).thenReturn(List.of(validEgg));
        when(customerPort.loadByCustomerCodeForUpdate("CUS88")).thenReturn(Optional.of(cleanCustomer));
        when(accountPort.pickAvailableAccountForUpdateSkipLocked("pool-uuid")).thenReturn(Optional.of(availableAccount));
        when(eggPort.loadEggsByOrderId("order-uuid")).thenReturn(List.of(validEgg));

        ClaimEggResponse response = claimService.claimEggReward("order-uuid", 2, "127.0.0.1");

        assertNotNull(response);
        assertEquals(1, response.getAccounts().size());
        assertEquals("gift_user", response.getAccounts().get(0).getUsername());
        assertEquals("Steam", response.getAccounts().get(0).getPlatform());
        assertEquals("A", response.getAccounts().get(0).getTier());
    }

    @Test
    void claimEggReward_OrderNotDelivered_ThrowsException() {
        validOrder.setDeliveryStatus("Đang giao hàng");
        when(eggPort.loadEggsForClaim("order-uuid", 1)).thenReturn(List.of(validEgg));

        BusinessRuleViolationException exception = assertThrows(BusinessRuleViolationException.class, () -> {
            claimService.claimEggReward("order-uuid", 1, "127.0.0.1");
        });

        assertEquals("Đơn hàng chưa được giao thành công.", exception.getMessage());
    }

    @Test
    void claimEggReward_Amnesty_ResetWarningStreak() {
        // Warning customer tries to claim Egg 1
        validEgg.setEggType(1);
        validEgg.setStatus("READY_TO_CLAIM");
        validEgg.setHatchAt(LocalDateTime.now().minusMinutes(5)); // Warning customer egg 1 has hatch time
        validOrder.setDeliveryStatus("Đã giao hàng");
        validOrder.setUpdatedAt(LocalDateTime.now().minusDays(20)); // Absolute success

        when(eggPort.loadEggsForClaim("order-uuid", 1)).thenReturn(List.of(validEgg));
        when(customerPort.loadByCustomerCodeForUpdate("CUS88")).thenReturn(Optional.of(warningCustomer));
        when(accountPort.pickAvailableAccountForUpdateSkipLocked("pool-uuid")).thenReturn(Optional.of(availableAccount));

        // Mock two orders post-return that are fully claimed and successful
        KiotvietOrder order1 = KiotvietOrder.builder()
                .id("order-1")
                .customerCode("CUS88")
                .deliveryStatus("Đã giao hàng")
                .createdAt(LocalDateTime.now().minusDays(20))
                .updatedAt(LocalDateTime.now().minusDays(20))
                .build();

        KiotvietOrder order2 = KiotvietOrder.builder()
                .id("order-2")
                .customerCode("CUS88")
                .deliveryStatus("Đã giao hàng")
                .createdAt(LocalDateTime.now().minusDays(20))
                .updatedAt(LocalDateTime.now().minusDays(20))
                .build();

        // Warning customer history
        when(orderPort.findByCustomerCode("CUS88")).thenReturn(List.of(validOrder, order1, order2));

        // Mock eggs for post orders: all are claimed
        Egg eggO1 = Egg.builder().status("CLAIMED").build();
        Egg eggO2 = Egg.builder().status("CLAIMED").build();

        // Stub eggPort.loadEggsByOrderId
        when(eggPort.loadEggsByOrderId("order-uuid")).thenReturn(List.of(validEgg)); // current egg
        when(eggPort.loadEggsByOrderId("order-1")).thenReturn(List.of(eggO1));
        when(eggPort.loadEggsByOrderId("order-2")).thenReturn(List.of(eggO2));

        // Stub customerPort.saveCustomer to verify amnesty reset
        claimService.claimEggReward("order-uuid", 1, "127.0.0.1");

        // Verify return streak is reset to 0
        verify(customerPort, times(2)).saveCustomer(argThat(cus -> cus.getReturnStreak() == 0 && "TRUSTED_1".equals(cus.getStatus())));
    }

    @Test
    void claimEggReward_AlreadyClaimed_ReturnsAccount() {
        validEgg.setStatus("CLAIMED");
        validEgg.setAccount(availableAccount);
        when(eggPort.loadEggsForClaim("order-uuid", 1)).thenReturn(List.of(validEgg));

        ClaimEggResponse response = claimService.claimEggReward("order-uuid", 1, "127.0.0.1");

        assertNotNull(response);
        assertEquals(1, response.getAccounts().size());
        assertEquals("gift_user", response.getAccounts().get(0).getUsername());
        assertEquals("Steam", response.getAccounts().get(0).getPlatform());
        assertEquals("A", response.getAccounts().get(0).getTier());
        assertTrue(response.getMessage().contains("danh sách thông tin"));
    }

    @Test
    void claimEggReward_EggType2_Success_RewardsCredits() {
        validEgg.setEggType(2);
        validEgg.setStatus("READY_TO_CLAIM");
        validEgg.setHatchAt(LocalDateTime.now().minusMinutes(5));
        when(eggPort.loadEggsForClaim("order-uuid", 2)).thenReturn(List.of(validEgg));
        when(customerPort.loadByCustomerCodeForUpdate("CUS88")).thenReturn(Optional.of(cleanCustomer));
        when(accountPort.pickAvailableAccountForUpdateSkipLocked("pool-uuid")).thenReturn(Optional.of(availableAccount));
        when(eggPort.loadEggsByOrderId("order-uuid")).thenReturn(List.of(validEgg));

        claimService.claimEggReward("order-uuid", 2, "127.0.0.1");

        assertEquals(2, cleanCustomer.getEarlyHatchCredits());
    }

    @Test
    void claimEggReward_EggType1_Success_DoesNotRewardCredits() {
        validEgg.setEggType(1);
        validEgg.setStatus("READY_TO_CLAIM");
        validEgg.setHatchAt(null);
        cleanCustomer.setEarlyHatchCredits(0);
        when(eggPort.loadEggsForClaim("order-uuid", 1)).thenReturn(List.of(validEgg));
        when(customerPort.loadByCustomerCodeForUpdate("CUS88")).thenReturn(Optional.of(cleanCustomer));
        when(accountPort.pickAvailableAccountForUpdateSkipLocked("pool-uuid")).thenReturn(Optional.of(availableAccount));
        when(eggPort.loadEggsByOrderId("order-uuid")).thenReturn(List.of(validEgg));

        claimService.claimEggReward("order-uuid", 1, "127.0.0.1");

        assertEquals(0, cleanCustomer.getEarlyHatchCredits());
    }
}