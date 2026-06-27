package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.UpdateCustomerStatusUseCase;
import com.project.anhgagifcode.application.port.in.dto.CustomerDto;
import com.project.anhgagifcode.application.port.in.dto.UpdateCustomerStatusRequest;
import com.project.anhgagifcode.application.port.out.CustomerPersistencePort;
import com.project.anhgagifcode.domain.exception.ResourceNotFoundException;
import com.project.anhgagifcode.domain.model.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class UpdateCustomerStatusService implements UpdateCustomerStatusUseCase {

    private final CustomerPersistencePort customerPort;

    @Override
    @Transactional
    public CustomerDto updateCustomerStatus(String customerCode, UpdateCustomerStatusRequest request) {
        Customer customer = customerPort.loadByCustomerCode(customerCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng này."));

        customer.setStatus(request.getStatus());
        if (request.getReturnStreak() != null) {
            customer.setReturnStreak(request.getReturnStreak());
        }
        if (request.getSuccessCount() != null) {
            customer.setSuccessCount(request.getSuccessCount());
        }

        Customer savedCustomer = customerPort.saveCustomer(customer);

        return CustomerDto.builder()
                .id(savedCustomer.getId())
                .customerCode(savedCustomer.getCustomerCode())
                .customerName(savedCustomer.getCustomerName())
                .status(savedCustomer.getStatus())
                .successCount(savedCustomer.getSuccessCount())
                .returnStreak(savedCustomer.getReturnStreak())
                .warningCount(savedCustomer.getWarningCount())
                .createdAt(savedCustomer.getCreatedAt())
                .build();
    }
}
