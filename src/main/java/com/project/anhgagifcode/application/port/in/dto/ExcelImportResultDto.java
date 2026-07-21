package com.project.anhgagifcode.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class ExcelImportResultDto {
    private int totalRows;
    private int successCount;
    private int duplicateInFileCount;
    private int duplicateInDbCount;
    private List<String> duplicateInFileUsernames;
    private List<String> duplicateInDbUsernames;
}
