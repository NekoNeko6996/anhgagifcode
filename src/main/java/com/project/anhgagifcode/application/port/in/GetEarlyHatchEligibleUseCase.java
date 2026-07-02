package com.project.anhgagifcode.application.port.in;

import com.project.anhgagifcode.application.port.in.dto.EarlyHatchGroupDto;
import java.util.List;

public interface GetEarlyHatchEligibleUseCase {
    List<EarlyHatchGroupDto> getEligibleItems();
}
