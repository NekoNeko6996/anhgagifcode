package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.AddGiftAccountUseCase;
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
        GiftAccount account = GiftAccount.builder()
                .id(UUID.randomUUID().toString())
                .username(request.getUsername())
                .password(request.getPassword())
                .tier(request.getTier())
                .token(request.getToken())
                .platform(request.getPlatform() != null ? request.getPlatform() : "ROBLOX")
                .status("AVAILABLE")
                .createdAt(LocalDateTime.now())
                .build();
        accountPort.save(account);
    }

    @Override
    @Transactional
    public int importAccountsFromExcel(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessRuleViolationException("File tải lên trống.");
        }
        
        List<GiftAccount> accounts = new ArrayList<>();
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // Lấy sheet đầu tiên
            boolean isHeader = true;

            for (Row row : sheet) {
                if (isHeader) {
                    isHeader = false;
                    continue; // Bỏ qua dòng tiêu đề
                }

                // Cột 1: Tài khoản, Cột 2: Mật khẩu, Cột 3: Token, Cột 4: Tier (Cột 0 là STT)
                String username = getCellValueAsString(row.getCell(1));
                String password = getCellValueAsString(row.getCell(2));
                
                // Nếu username rỗng -> Bỏ qua dòng này (có thể là dòng trống cuối file)
                if (username == null || username.trim().isEmpty()) {
                    continue;
                }

                String token = getCellValueAsString(row.getCell(3));
                String tier = getCellValueAsString(row.getCell(4));

                GiftAccount account = GiftAccount.builder()
                        .id(UUID.randomUUID().toString())
                        .username(username)
                        .password(password)
                        .token(token)
                        .tier(tier != null && !tier.isEmpty() ? tier : "D") // Mặc định Tier D nếu thiếu
                        .status("AVAILABLE")
                        .platform("ROBLOX")
                        .createdAt(LocalDateTime.now())
                        .build();
                accounts.add(account);
            }
            
            if (accounts.isEmpty()) {
                throw new BusinessRuleViolationException("Không tìm thấy dữ liệu hợp lệ trong file Excel.");
            }
            
            // Lưu hàng loạt vào DB
            accountPort.saveAll(accounts);
            return accounts.size();

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