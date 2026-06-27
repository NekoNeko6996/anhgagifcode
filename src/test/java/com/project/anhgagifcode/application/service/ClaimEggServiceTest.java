package com.project.anhgagifcode.application.service;

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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
    private KiotvietApiPort apiPort;
    @Mock
    private CustomerPersistencePort customerPort;

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
                .deliveryStatus("Đang giao hàng")
                .lastSyncedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now().minusDays(20))
                .build();

        validEgg = Egg.builder()
                .id("egg-uuid")
                .eggType(1)
                .status("PENDING")
                .giftPool(GiftPool.builder().id("pool-uuid").build())
                .order(validOrder)
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
    }

    @Test
    void claimEggReward_Success_CleanCustomer_Egg1_Immediate() {
        when(eggPort.loadEggForUpdate("egg-uuid")).thenReturn(Optional.of(validEgg));
        when(customerPort.loadByCustomerCode("CUS88")).thenReturn(Optional.of(cleanCustomer));
        when(accountPort.countAvailableAccountsByPoolId("pool-uuid")).thenReturn(10L);
        when(accountPort.pickRandomAvailableAccountForUpdate(eq("pool-uuid"), anyInt())).thenReturn(Optional.of(availableAccount));

        ClaimEggResponse response = claimService.claimEggReward("egg-uuid", "127.0.0.1");

        assertNotNull(response);
        assertEquals("gift_user", response.getUsername());
        assertEquals("Steam", response.getPlatform());

        verify(accountPort, times(1)).updateAccount(argThat(acc -> "ASSIGNED".equals(acc.getStatus())));
        verify(eggPort, times(1)).saveEgg(argThat(egg -> "CLAIMED".equals(egg.getStatus())));
        verify(logPort, times(1)).saveLog(any(EggOpeningLog.class));
    }

    @Test
    void claimEggReward_BannedCustomer_ThrowsException() {
        when(eggPort.loadEggForUpdate("egg-uuid")).thenReturn(Optional.of(validEgg));
        when(customerPort.loadByCustomerCode("CUS88")).thenReturn(Optional.of(bannedCustomer));

        BusinessRuleViolationException exception = assertThrows(BusinessRuleViolationException.class, () -> {
            claimService.claimEggReward("egg-uuid", "127.0.0.1");
        });

        assertEquals("Tài khóa bị khóa do vi phạm chính sách", exception.getMessage().replace("Tài khoản", "Tài khóa"));
    }

    @Test
    void claimEggReward_HatchingCooldown_ThrowsException() {
        validEgg.setEggType(2);
        validEgg.setHatchAt(LocalDateTime.now().plusDays(2)); // Hatch date in future
        when(eggPort.loadEggForUpdate("egg-uuid")).thenReturn(Optional.of(validEgg));
        when(customerPort.loadByCustomerCode("CUS88")).thenReturn(Optional.of(cleanCustomer));

        BusinessRuleViolationException exception = assertThrows(BusinessRuleViolationException.class, () -> {
            claimService.claimEggReward("egg-uuid", "127.0.0.1");
        });

        assertTrue(exception.getMessage().contains("chưa đến thời gian"));
    }

    @Test
    void claimEggReward_Egg2_NotAbsoluteSuccess_ThrowsException() {
        validEgg.setEggType(2);
        validEgg.setHatchAt(LocalDateTime.now().minusMinutes(5)); // Cooldown finished
        // Order is still "Đang giao hàng" (Not "Đã giao hàng")
        when(eggPort.loadEggForUpdate("egg-uuid")).thenReturn(Optional.of(validEgg));
        when(customerPort.loadByCustomerCode("CUS88")).thenReturn(Optional.of(cleanCustomer));

        BusinessRuleViolationException exception = assertThrows(BusinessRuleViolationException.class, () -> {
            claimService.claimEggReward("egg-uuid", "127.0.0.1");
        });

        assertTrue(exception.getMessage().contains("chưa đạt trạng thái thành công tuyệt đối"));
    }

    @Test
    void claimEggReward_Amnesty_ResetWarningStreak() {
        // Warning customer tries to claim Egg 1
        validEgg.setEggType(1);
        validEgg.setHatchAt(LocalDateTime.now().minusMinutes(5)); // Warning customer egg 1 has hatch time
        validOrder.setDeliveryStatus("Đã giao hàng");
        validOrder.setUpdatedAt(LocalDateTime.now().minusDays(20)); // Absolute success

        when(eggPort.loadEggForUpdate("egg-uuid")).thenReturn(Optional.of(validEgg));
        when(customerPort.loadByCustomerCode("CUS88")).thenReturn(Optional.of(warningCustomer));
        when(accountPort.countAvailableAccountsByPoolId("pool-uuid")).thenReturn(10L);
        when(accountPort.pickRandomAvailableAccountForUpdate(eq("pool-uuid"), anyInt())).thenReturn(Optional.of(availableAccount));

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
        claimService.claimEggReward("egg-uuid", "127.0.0.1");

        // Verify return streak is reset to 0
        verify(customerPort, times(1)).saveCustomer(argThat(cus -> cus.getReturnStreak() == 0 && "TRUSTED_1".equals(cus.getStatus())));
    }
}