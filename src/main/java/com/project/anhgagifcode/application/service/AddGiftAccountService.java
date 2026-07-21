package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.AddGiftAccountUseCase;
import com.project.anhgagifcode.application.port.in.dto.ExcelImportResultDto;
import com.project.anhgagifcode.application.port.out.GiftAccountPersistencePort;
import com.project.anhgagifcode.domain.exception.BusinessRuleViolationException;
import com.project.anhgagifcode.domain.model.GiftAccount;
import com.project.anhgagifcode.infrastructure.adapter.in.web.dto.CreateGiftAccountRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddGiftAccountService implements AddGiftAccountUseCase {

    private final GiftAccountPersistencePort accountPort;

    @Override
    @Transactional
    public void addSingleAccount(CreateGiftAccountRequest request) {
        String trimmedUsername = request.getUsername().trim();
        String trimmedPlatform = request.getPlatform() != null ? request.getPlatform().trim() : "ROBLOX";
        if (accountPort.existsByUsernameAndPlatform(trimmedUsername, trimmedPlatform)) {
            throw new BusinessRuleViolationException("Tài khoản '" + trimmedUsername + "' với nền tảng '" + trimmedPlatform + "' đã tồn tại trong hệ thống.");
        }

        GiftAccount account = GiftAccount.builder()
                .id(UUID.randomUUID().toString())
                .username(trimmedUsername)
                .password(request.getPassword())
                .tier(request.getTier())
                .token(request.getToken())
                .platform(trimmedPlatform)
                .status("AVAILABLE")
                .createdAt(LocalDateTime.now())
                .build();
        accountPort.save(account);
    }

    @Override
    @Transactional
    public ExcelImportResultDto importAccountsFromExcel(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessRuleViolationException("File tải lên trống.");
        }
        
        List<GiftAccount> potentialAccounts = new ArrayList<>();
        List<String> usernamesInFile = new ArrayList<>();
        List<String> duplicatesInFile = new ArrayList<>();
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // Lấy sheet đầu tiên
            boolean isHeader = true;
            int totalRowsCount = 0;

            int usernameIdx = 1;
            int passwordIdx = 2;
            int platformIdx = -1;
            int tierIdx = -1;
            int tokenIdx = -1;

            Row firstRow = sheet.getRow(0);
            if (firstRow != null) {
                for (int c = 0; c < firstRow.getLastCellNum(); c++) {
                    Cell cell = firstRow.getCell(c);
                    String headerVal = getCellValueAsString(cell);
                    if (headerVal == null) continue;
                    headerVal = headerVal.trim().toLowerCase();

                    if (headerVal.contains("username") || headerVal.contains("tài khoản") || headerVal.contains("tk")) {
                        usernameIdx = c;
                    } else if (headerVal.contains("password") || headerVal.contains("mật khẩu") || headerVal.contains("mk")) {
                        passwordIdx = c;
                    } else if (headerVal.contains("platform") || headerVal.contains("nền tảng")) {
                        platformIdx = c;
                    } else if (headerVal.contains("tier") || headerVal.contains("phân cấp") || headerVal.contains("loại")) {
                        tierIdx = c;
                    } else if (headerVal.contains("token") || headerVal.contains("mã quà") || headerVal.contains("mã")) {
                        tokenIdx = c;
                    }
                }
            }

            for (Row row : sheet) {
                if (isHeader) {
                    isHeader = false;
                    continue; // Bỏ qua dòng tiêu đề
                }

                String username = usernameIdx != -1 ? getCellValueAsString(row.getCell(usernameIdx)) : null;
                String password = passwordIdx != -1 ? getCellValueAsString(row.getCell(passwordIdx)) : null;
                
                // Nếu username rỗng -> Bỏ qua dòng này (có thể là dòng trống cuối file)
                if (username == null || username.trim().isEmpty()) {
                    continue;
                }

                totalRowsCount++;
                String trimmedUsername = username.trim();
                String platformVal = platformIdx != -1 ? getCellValueAsString(row.getCell(platformIdx)) : null;
                String trimmedPlatform = platformVal != null && !platformVal.trim().isEmpty() ? platformVal.trim() : "ROBLOX";
                String tierVal = tierIdx != -1 ? getCellValueAsString(row.getCell(tierIdx)) : null;
                String trimmedTier = tierVal != null && !tierVal.trim().isEmpty() ? tierVal.trim() : "D";
                if (trimmedTier.length() > 10) {
                    trimmedTier = trimmedTier.substring(0, 10);
                }
                String token = tokenIdx != -1 ? getCellValueAsString(row.getCell(tokenIdx)) : null;

                String compoundKey = trimmedUsername.toLowerCase() + "|||" + trimmedPlatform.toLowerCase();

                // Kiểm tra trùng lặp trong file
                if (usernamesInFile.contains(compoundKey)) {
                    String dupDisplay = trimmedUsername + " (" + trimmedPlatform + ")";
                    if (!duplicatesInFile.contains(dupDisplay)) {
                        duplicatesInFile.add(dupDisplay);
                    }
                } else {
                    usernamesInFile.add(compoundKey);
                    
                    GiftAccount account = GiftAccount.builder()
                            .id(UUID.randomUUID().toString())
                            .username(trimmedUsername)
                            .password(password)
                            .token(token)
                            .tier(trimmedTier)
                            .status("AVAILABLE")
                            .platform(trimmedPlatform)
                            .createdAt(LocalDateTime.now())
                            .build();
                    potentialAccounts.add(account);
                }
            }
            
            if (totalRowsCount == 0) {
                throw new BusinessRuleViolationException("Không tìm thấy dữ liệu hợp lệ trong file Excel.");
            }

            // Kiểm tra trùng lặp với DB từ các tài khoản không trùng trong file
            List<String> potentialUsernames = potentialAccounts.stream()
                    .map(GiftAccount::getUsername)
                    .toList();
            
            List<GiftAccount> existingDbAccounts = potentialUsernames.isEmpty() ? List.of() : accountPort.findByUsernameIn(potentialUsernames);
            
            java.util.Set<String> existingDbKeysLower = existingDbAccounts.stream()
                    .map(acc -> acc.getUsername().toLowerCase() + "|||" + acc.getPlatform().toLowerCase())
                    .collect(java.util.stream.Collectors.toSet());

            List<GiftAccount> accountsToSave = new ArrayList<>();
            List<String> duplicatesInDb = new ArrayList<>();

            for (GiftAccount account : potentialAccounts) {
                String compoundKey = account.getUsername().toLowerCase() + "|||" + account.getPlatform().toLowerCase();
                if (existingDbKeysLower.contains(compoundKey)) {
                    duplicatesInDb.add(account.getUsername() + " (" + account.getPlatform() + ")");
                } else {
                    accountsToSave.add(account);
                }
            }
            
            // Lưu các tài khoản hợp lệ (không trùng lặp) vào DB
            if (!accountsToSave.isEmpty()) {
                accountPort.saveAll(accountsToSave);
            }
            
            return ExcelImportResultDto.builder()
                    .totalRows(totalRowsCount)
                    .successCount(accountsToSave.size())
                    .duplicateInFileCount(duplicatesInFile.size())
                    .duplicateInDbCount(duplicatesInDb.size())
                    .duplicateInFileUsernames(duplicatesInFile)
                    .duplicateInDbUsernames(duplicatesInDb)
                    .build();

        } catch (BusinessRuleViolationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Lỗi khi đọc file Excel: ", e);
            throw new BusinessRuleViolationException("Lỗi xử lý file Excel: " + e.getMessage());
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return String.valueOf((long) cell.getNumericCellValue()); // Ép kiểu phòng hờ nhập pass bằng số
            default: return "";
        }
    }
}