package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.GetCustomersUseCase;
import com.project.anhgagifcode.application.port.in.dto.CustomerDto;
import com.project.anhgagifcode.application.port.out.CustomerPersistencePort;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GetCustomersService implements GetCustomersUseCase {

    private final CustomerPersistencePort customerPersistencePort;

    @Override
    public List<CustomerDto> getCustomers() {
        return customerPersistencePort.findAll().stream()
                .map(c -> CustomerDto.builder()
                        .id(c.getId())
                        .customerCode(c.getCustomerCode())
                        .customerName(c.getCustomerName())
                        .status(c.getStatus())
                        .successCount(c.getSuccessCount())
                        .returnStreak(c.getReturnStreak())
                        .warningCount(c.getWarningCount())
                        .earlyHatchCredits(c.getEarlyHatchCredits())
                        .returnCount(c.getReturnCount())
                        .createdAt(c.getCreatedAt())
                        .updatedAt(c.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
