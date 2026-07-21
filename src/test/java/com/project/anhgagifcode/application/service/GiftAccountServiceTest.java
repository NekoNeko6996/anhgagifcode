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

    @Test
    void testAddSingleAccount_Duplicate_ThrowsException() {
        AddGiftAccountService service = new AddGiftAccountService(accountPort);
        CreateGiftAccountRequest request = new CreateGiftAccountRequest();
        request.setUsername("roblox_user");
        request.setPassword("pass123");
        request.setTier("B");
        request.setPlatform("Roblox");

        when(accountPort.existsByUsernameAndPlatform("roblox_user", "Roblox")).thenReturn(true);

        assertThrows(BusinessRuleViolationException.class, () -> service.addSingleAccount(request));
        verify(accountPort, never()).save(any(GiftAccount.class));
    }

    @Test
    void testUpdateGiftAccount_DuplicateUsername_ThrowsException() {
        UpdateGiftAccountService service = new UpdateGiftAccountService(accountPort);
        UpdateGiftAccountRequest request = UpdateGiftAccountRequest.builder()
                .username("existing_user")
                .password("updated_pass")
                .platform("Roblox")
                .status("AVAILABLE")
                .build();

        when(accountPort.findById("acc-1")).thenReturn(Optional.of(mockAccount));
        when(accountPort.existsByUsernameAndPlatformAndIdNot("existing_user", "Roblox", "acc-1")).thenReturn(true);

        assertThrows(BusinessRuleViolationException.class, () -> service.updateGiftAccount("acc-1", request));
        verify(accountPort, never()).save(any(GiftAccount.class));
    }

    @Test
    void testImportFromExcel_Success() throws Exception {
        AddGiftAccountService service = new AddGiftAccountService(accountPort);
        String[][] data = {
            {"user1", "pass1", "Roblox", "A", "tok1"},
            {"user2", "pass2", "Steam", "B", "tok2"}
        };
        MockMultipartFile file = createExcelFile(data);

        when(accountPort.findByUsernameIn(any())).thenReturn(List.of());

        com.project.anhgagifcode.application.port.in.dto.ExcelImportResultDto report = service.importAccountsFromExcel(file);

        assertEquals(2, report.getTotalRows());
        assertEquals(2, report.getSuccessCount());
        assertEquals(0, report.getDuplicateInFileCount());
        assertEquals(0, report.getDuplicateInDbCount());
        verify(accountPort, times(1)).saveAll(any());
    }

    @Test
    void testImportFromExcel_DuplicateInFile_Success() throws Exception {
        AddGiftAccountService service = new AddGiftAccountService(accountPort);
        String[][] data = {
            {"user1", "pass1", "Roblox", "A", "tok1"},
            {"user1", "pass2", "Roblox", "B", "tok2"}
        };
        MockMultipartFile file = createExcelFile(data);

        when(accountPort.findByUsernameIn(any())).thenReturn(List.of());

        com.project.anhgagifcode.application.port.in.dto.ExcelImportResultDto report = service.importAccountsFromExcel(file);

        assertEquals(2, report.getTotalRows());
        assertEquals(1, report.getSuccessCount());
        assertEquals(1, report.getDuplicateInFileCount());
        assertEquals(0, report.getDuplicateInDbCount());
        assertEquals("user1 (Roblox)", report.getDuplicateInFileUsernames().get(0));
        verify(accountPort, times(1)).saveAll(any());
    }

    @Test
    void testImportFromExcel_DuplicateInDb_Success() throws Exception {
        AddGiftAccountService service = new AddGiftAccountService(accountPort);
        String[][] data = {
            {"user1", "pass1", "Roblox", "A", "tok1"},
            {"user2", "pass2", "Steam", "B", "tok2"}
        };
        MockMultipartFile file = createExcelFile(data);

        when(accountPort.findByUsernameIn(any())).thenReturn(List.of(
            GiftAccount.builder().username("user1").platform("Roblox").build()
        ));

        com.project.anhgagifcode.application.port.in.dto.ExcelImportResultDto report = service.importAccountsFromExcel(file);

        assertEquals(2, report.getTotalRows());
        assertEquals(1, report.getSuccessCount());
        assertEquals(0, report.getDuplicateInFileCount());
        assertEquals(1, report.getDuplicateInDbCount());
        assertEquals("user1 (Roblox)", report.getDuplicateInDbUsernames().get(0));
        verify(accountPort, times(1)).saveAll(any());
    }

    @Test
    void testImportFromExcel_SameUsernameDifferentPlatform_Success() throws Exception {
        AddGiftAccountService service = new AddGiftAccountService(accountPort);
        String[][] data = {
            {"user1", "pass1", "Roblox", "A", "tok1"},
            {"user1", "pass2", "Steam", "B", "tok2"}
        };
        MockMultipartFile file = createExcelFile(data);

        when(accountPort.findByUsernameIn(any())).thenReturn(List.of());

        com.project.anhgagifcode.application.port.in.dto.ExcelImportResultDto report = service.importAccountsFromExcel(file);

        assertEquals(2, report.getTotalRows());
        assertEquals(2, report.getSuccessCount());
        assertEquals(0, report.getDuplicateInFileCount());
        assertEquals(0, report.getDuplicateInDbCount());
        verify(accountPort, times(1)).saveAll(any());
    }

    private MockMultipartFile createExcelFile(String[][] data) throws java.io.IOException {
        org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet();
        
        org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("STT");
        header.createCell(1).setCellValue("Tài khoản");
        header.createCell(2).setCellValue("Mật khẩu");
        header.createCell(3).setCellValue("Platform");
        header.createCell(4).setCellValue("Tier");
        header.createCell(5).setCellValue("Token");

        for (int i = 0; i < data.length; i++) {
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(i + 1);
            row.createCell(1).setCellValue(data[i][0]);
            row.createCell(2).setCellValue(data[i][1]);
            row.createCell(3).setCellValue(data[i][2]);
            row.createCell(4).setCellValue(data[i][3]);
            row.createCell(5).setCellValue(data[i][4]);
        }

        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();

        return new MockMultipartFile("file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bos.toByteArray());
    }
}

