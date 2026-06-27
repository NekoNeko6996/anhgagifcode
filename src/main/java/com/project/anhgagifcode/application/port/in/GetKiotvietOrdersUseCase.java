package com.project.anhgagifcode.application.port.in;

import com.project.anhgagifcode.application.port.in.dto.KiotvietOrderDto;
import java.util.List;

public interface GetKiotvietOrdersUseCase {
    List<KiotvietOrderDto> getOrders();
}
