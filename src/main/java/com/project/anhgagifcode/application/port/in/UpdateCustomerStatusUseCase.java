package com.project.anhgagifcode.application.port.in;

import com.project.anhgagifcode.application.port.in.dto.UpdateCustomerStatusRequest;
import com.project.anhgagifcode.application.port.in.dto.CustomerDto;

public interface UpdateCustomerStatusUseCase {
    CustomerDto updateCustomerStatus(String customerCode, UpdateCustomerStatusRequest request);
}
