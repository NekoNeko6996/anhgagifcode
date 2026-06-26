package com.project.anhgagifcode.application.port.in;

import com.project.anhgagifcode.application.port.in.dto.SyncOrderResponse;

public interface SyncKiotvietOrderUseCase {
    SyncOrderResponse syncAndGetOrderDetails(String orderCode);
}