package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.dto.*;
import com.project.anhgagifcode.application.port.out.GiftAccountPersistencePort;
import com.project.anhgagifcode.application.port.out.GiftPoolPersistencePort;
import com.project.anhgagifcode.application.port.out.PoolAccountMappingPersistencePort;
import com.project.anhgagifcode.domain.exception.BusinessRuleViolationException;
import com.project.anhgagifcode.domain.exception.ResourceNotFoundException;
import com.project.anhgagifcode.domain.model.GiftAccount;
import com.project.anhgagifcode.domain.model.GiftPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GiftPoolServiceTest {

    @Mock
    private GiftPoolPersistencePort poolPort;
    @Mock
    private GiftAccountPersistencePort accountPort;
    @Mock
    private PoolAccountMappingPersistencePort mappingPort;

    private GiftPool mockPool;
    private GiftAccount mockAccount;

    @BeforeEach
    void setUp() {
        mockPool = GiftPool.builder()
                .id("pool-1")
                .poolName("Pool Roblox")
                .tier("A")
                .createdAt(LocalDateTime.now())
                .build();

        mockAccount = GiftAccount.builder()
                .id("acc-1")
                .username("roblox_user")
                .password("pass123")
                .platform("Roblox")
                .status("AVAILABLE")
                .build();
    }

    @Test
    void testAddAccountToPool_Success() {
        AddAccountToPoolService service = new AddAccountToPoolService(mappingPort);
        AddAccountToPoolRequest request = AddAccountToPoolRequest.builder()
                .poolId("pool-1")
                .accountId("acc-1")
                .build();

        when(mappingPort.existsByPoolIdAndAccountId("pool-1", "acc-1")).thenReturn(false);

        service.addAccountToPool(request);

        verify(mappingPort, times(1)).saveMapping("pool-1", "acc-1");
    }

    @Test
    void testAddAccountToPool_AlreadyExists_ThrowsException() {
        AddAccountToPoolService service = new AddAccountToPoolService(mappingPort);
        AddAccountToPoolRequest request = AddAccountToPoolRequest.builder()
                .poolId("pool-1")
                .accountId("acc-1")
                .build();

        when(mappingPort.existsByPoolIdAndAccountId("pool-1", "acc-1")).thenReturn(true);

        assertThrows(BusinessRuleViolationException.class, () -> service.addAccountToPool(request));
    }

    @Test
    void testAddAccountsToPool_Success() {
        AddAccountsToPoolService service = new AddAccountsToPoolService(mappingPort);
        AddAccountsToPoolRequest request = AddAccountsToPoolRequest.builder()
                .poolId("pool-1")
                .accountIds(List.of("acc-1", "acc-2"))
                .build();

        service.addAccountsToPool(request);

        verify(mappingPort, times(1)).saveMappings("pool-1", List.of("acc-1", "acc-2"));
    }

    @Test
    void testCreatePool_Success() {
        CreateGiftPoolService service = new CreateGiftPoolService(poolPort);
        CreateGiftPoolRequest request = CreateGiftPoolRequest.builder()
                .poolName("New Pool")
                .tier("B")
                .build();

        when(poolPort.savePool(any(GiftPool.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GiftPoolDto result = service.createPool(request);

        assertNotNull(result);
        assertEquals("New Pool", result.getPoolName());
        assertEquals("B", result.getTier());
    }

    @Test
    void testGetGiftPoolDetail_Success() {
        GetGiftPoolDetailService service = new GetGiftPoolDetailService(poolPort, accountPort);
        when(poolPort.findById("pool-1")).thenReturn(Optional.of(mockPool));
        when(accountPort.findAccountsByPoolId("pool-1")).thenReturn(List.of(mockAccount));

        GiftPoolDetailDto detail = service.getPoolDetail("pool-1");

        assertNotNull(detail);
        assertEquals("pool-1", detail.getId());
        assertEquals(1, detail.getAccounts().size());
        assertEquals("roblox_user", detail.getAccounts().get(0).getUsername());
    }

    @Test
    void testGetGiftPoolDetail_NotFound_ThrowsException() {
        GetGiftPoolDetailService service = new GetGiftPoolDetailService(poolPort, accountPort);
        when(poolPort.findById("invalid-pool")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getPoolDetail("invalid-pool"));
    }

    @Test
    void testGetGiftPools_Success() {
        GetGiftPoolsService service = new GetGiftPoolsService(poolPort);
        when(poolPort.findAll()).thenReturn(List.of(mockPool));

        List<GiftPoolDto> pools = service.getGiftPools();

        assertEquals(1, pools.size());
        assertEquals("Pool Roblox", pools.get(0).getPoolName());
    }

    @Test
    void testRemoveAccountsFromPool_Success() {
        RemoveAccountsFromPoolService service = new RemoveAccountsFromPoolService(mappingPort);
        RemoveAccountsFromPoolRequest request = RemoveAccountsFromPoolRequest.builder()
                .poolId("pool-1")
                .accountIds(List.of("acc-1"))
                .build();

        service.removeAccountsFromPool(request);

        verify(mappingPort, times(1)).removeMappings("pool-1", List.of("acc-1"));
    }

    @Test
    void testRemoveGiftPool_Success() {
        RemoveGiftPoolService service = new RemoveGiftPoolService(poolPort);
        when(poolPort.existsById("pool-1")).thenReturn(true);
        when(poolPort.hasAssociatedEggs("pool-1")).thenReturn(false);

        service.removePool("pool-1");

        verify(poolPort, times(1)).deletePool("pool-1");
    }

    @Test
    void testRemoveGiftPool_HasActiveEggs_ThrowsException() {
        RemoveGiftPoolService service = new RemoveGiftPoolService(poolPort);
        when(poolPort.existsById("pool-1")).thenReturn(true);
        when(poolPort.hasAssociatedEggs("pool-1")).thenReturn(true);

        assertThrows(BusinessRuleViolationException.class, () -> service.removePool("pool-1"));
    }

    @Test
    void testUpdateGiftPool_Success() {
        UpdateGiftPoolService service = new UpdateGiftPoolService(poolPort);
        UpdateGiftPoolRequest request = UpdateGiftPoolRequest.builder()
                .poolName("Updated Pool Name")
                .tier("Z")
                .build();

        when(poolPort.findById("pool-1")).thenReturn(Optional.of(mockPool));
        when(poolPort.savePool(any(GiftPool.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GiftPoolDto updated = service.updatePool("pool-1", request);

        assertNotNull(updated);
        assertEquals("Updated Pool Name", updated.getPoolName());
        assertEquals("Z", updated.getTier());
    }
}
