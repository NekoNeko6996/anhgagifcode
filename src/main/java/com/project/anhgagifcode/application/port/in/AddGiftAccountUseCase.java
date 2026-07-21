package com.project.anhgagifcode.application.port.in;

import com.project.anhgagifcode.application.port.in.dto.ExcelImportResultDto;
import com.project.anhgagifcode.infrastructure.adapter.in.web.dto.CreateGiftAccountRequest;
import org.springframework.web.multipart.MultipartFile;

public interface AddGiftAccountUseCase {
    void addSingleAccount(CreateGiftAccountRequest request);
    ExcelImportResultDto importAccountsFromExcel(MultipartFile file);
}