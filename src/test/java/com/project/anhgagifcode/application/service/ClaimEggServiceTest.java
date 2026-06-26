package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.dto.ClaimEggResponse;
import com.project.anhgagifcode.application.port.out.EggOpeningLogPersistencePort;
import com.project.anhgagifcode.application.port.out.EggPersistencePort;
import com.project.anhgagifcode.application.port.out.GiftAccountPersistencePort;
import com.project.anhgagifcode.domain.exception.BusinessRuleViolationException;
import com.project.anhgagifcode.domain.exception.ResourceNotFoundException;
import com.project.anhgagifcode.domain.model.Egg;
import com.project.anhgagifcode.domain.model.EggOpeningLog;
import com.project.anhgagifcode.domain.model.GiftAccount;
import com.project.anhgagifcode.domain.model.GiftPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimEggServiceTest {

    @Mock
    private EggPersistencePort eggPort;
    @Mock
    private GiftAccountPersistencePort accountPort;
    @Mock
    private EggOpeningLogPersistencePort logPort;

    @InjectMocks
    private ClaimEggService claimService;

    private Egg validEgg;
    private GiftAccount availableAccount;

    @BeforeEach
    void setUp() {
        validEgg = Egg.builder()
                .id("egg-uuid")
                .eggType(1)
                .status("READY_TO_CLAIM")
                .giftPool(GiftPool.builder().id("pool-uuid").build())
                .build();

        availableAccount = GiftAccount.builder()
                .id("acc-uuid")
                .username("test_user")
                .password("test_pass")
                .platform("Roblox")
                .status("AVAILABLE")
                .build();
    }

    @Test
    void claimEggReward_Success() {
        when(eggPort.loadEggForUpdate("egg-uuid")).thenReturn(Optional.of(validEgg));
        when(accountPort.countAvailableAccountsByPoolId("pool-uuid")).thenReturn(5L);
        when(accountPort.pickRandomAvailableAccountForUpdate(eq("pool-uuid"), anyInt())).thenReturn(Optional.of(availableAccount));

        ClaimEggResponse response = claimService.claimEggReward("egg-uuid", "127.0.0.1");

        assertNotNull(response);
        assertEquals("test_user", response.getUsername());
        assertEquals("Roblox", response.getPlatform());

        verify(accountPort, times(1)).updateAccount(argThat(acc -> "ASSIGNED".equals(acc.getStatus())));
        verify(eggPort, times(1)).saveEgg(argThat(egg -> "CLAIMED".equals(egg.getStatus())));
        verify(logPort, times(1)).saveLog(any(EggOpeningLog.class));
    }

    @Test
    void claimEggReward_EggNotFound_ThrowsException() {
        when(eggPort.loadEggForUpdate("invalid-egg")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            claimService.claimEggReward("invalid-egg", "127.0.0.1");
        });
    }

    @Test
    void claimEggReward_EggAlreadyClaimed_ThrowsException() {
        validEgg.setStatus("CLAIMED");
        when(eggPort.loadEggForUpdate("egg-uuid")).thenReturn(Optional.of(validEgg));

        BusinessRuleViolationException exception = assertThrows(BusinessRuleViolationException.class, () -> {
            claimService.claimEggReward("egg-uuid", "127.0.0.1");
        });

        assertTrue(exception.getMessage().contains("đã được mở"));
    }

    @Test
    void claimEggReward_EggIsStillHatching_ThrowsException() {
        validEgg.setEggType(2);
        validEgg.setHatchAt(LocalDateTime.now().plusHours(2)); // Còn 2 tiếng nữa mới nở
        when(eggPort.loadEggForUpdate("egg-uuid")).thenReturn(Optional.of(validEgg));

        BusinessRuleViolationException exception = assertThrows(BusinessRuleViolationException.class, () -> {
            claimService.claimEggReward("egg-uuid", "127.0.0.1");
        });

        assertTrue(exception.getMessage().contains("chưa đến thời gian"));
    }

    @Test
    void claimEggReward_OutOfStock_ThrowsException() {
        when(eggPort.loadEggForUpdate("egg-uuid")).thenReturn(Optional.of(validEgg));
        when(accountPort.countAvailableAccountsByPoolId("pool-uuid")).thenReturn(0L); // Hết quà

        BusinessRuleViolationException exception = assertThrows(BusinessRuleViolationException.class, () -> {
            claimService.claimEggReward("egg-uuid", "127.0.0.1");
        });

        assertTrue(exception.getMessage().contains("đã hết"));
        verify(accountPort, never()).pickRandomAvailableAccountForUpdate(anyString(), anyInt());
    }
}