package com.project.anhgagifcode.application.port.in;

import com.project.anhgagifcode.application.port.in.dto.CustomerDto;
import java.util.List;

public interface GetCustomersUseCase {
    List<CustomerDto> getCustomers();
}
