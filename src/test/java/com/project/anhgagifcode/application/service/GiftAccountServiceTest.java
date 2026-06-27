package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.dto.*;
import com.project.anhgagifcode.application.port.out.GiftAccountPersistencePort;
import com.project.anhgagifcode.domain.exception.BusinessRuleViolationException;
import com.project.anhgagifcode.domain.exception.ResourceNotFoundException;
import com.project.anhgagifcode.domain.model.GiftAccount;
import com.project.anhgagifcode.infrastructure.adapter.in.web.dto.CreateGiftAccountRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GiftAccountServiceTest {

    @Mock
    private GiftAccountPersistencePort accountPort;

    private GiftAccount mockAccount;

    @BeforeEach
    void setUp() {
        mockAccount = GiftAccount.builder()
                .id("acc-1")
                .username("roblox_user")
                .password("pass123")
                .platform("Roblox")
                .status("AVAILABLE")
                .tier("A")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testAddSingleAccount_Success() {
        AddGiftAccountService service = new AddGiftAccountService(accountPort);
        CreateGiftAccountRequest request = new CreateGiftAccountRequest();
        request.setUsername("new_user");
        request.setPassword("pass123");
        request.setTier("B");
        request.setPlatform("Roblox");

        service.addSingleAccount(request);

        verify(accountPort, times(1)).save(any(GiftAccount.class));
    }

    @Test
    void testImportFromExcel_EmptyFile_ThrowsException() {
        AddGiftAccountService service = new AddGiftAccountService(accountPort);
        MockMultipartFile file = new MockMultipartFile("file", new byte[0]);

        assertThrows(BusinessRuleViolationException.class, () -> service.importAccountsFromExcel(file));
    }

    @Test
    void testDeleteGiftAccounts_Success() {
        DeleteGiftAccountsService service = new DeleteGiftAccountsService(accountPort);
        DeleteGiftAccountsRequest request = DeleteGiftAccountsRequest.builder()
                .accountIds(List.of("acc-1"))
                .build();

        service.deleteAccounts(request);

        verify(accountPort, times(1)).deleteAccounts(List.of("acc-1"));
    }

    @Test
    void testGetGiftAccounts_Success() {
        GetGiftAccountsService service = new GetGiftAccountsService(accountPort);
        when(accountPort.findAll()).thenReturn(List.of(mockAccount));

        List<GiftAccountDto> result = service.getGiftAccounts();

        assertEquals(1, result.size());
        assertEquals("roblox_user", result.get(0).getUsername());
    }

    @Test
    void testUpdateGiftAccount_Success() {
        UpdateGiftAccountService service = new UpdateGiftAccountService(accountPort);
        UpdateGiftAccountRequest request = UpdateGiftAccountRequest.builder()
                .username("updated_user")
                .password("updated_pass")
                .platform("Steam")
                .status("ASSIGNED")
                .build();

        when(accountPort.findById("acc-1")).thenReturn(Optional.of(mockAccount));
        when(accountPort.save(any(GiftAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GiftAccountDto updated = service.updateGiftAccount("acc-1", request);

        assertNotNull(updated);
        assertEquals("updated_user", updated.getUsername());
        assertEquals("updated_pass", updated.getPassword());
        assertEquals("Steam", updated.getPlatform());
        assertEquals("ASSIGNED", updated.getStatus());
    }

    @Test
    void testUpdateGiftAccount_NotFound_ThrowsException() {
        UpdateGiftAccountService service = new UpdateGiftAccountService(accountPort);
        UpdateGiftAccountRequest request = UpdateGiftAccountRequest.builder()
                .username("updated_user")
                .password("updated_pass")
                .platform("Steam")
                .status("ASSIGNED")
                .build();

        when(accountPort.findById("invalid-acc")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.updateGiftAccount("invalid-acc", request));
    }
}
