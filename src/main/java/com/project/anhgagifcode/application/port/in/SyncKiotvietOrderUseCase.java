package com.project.anhgagifcode.application.port.in;

import com.project.anhgagifcode.application.port.in.dto.SyncOrderResponse;
import com.project.anhgagifcode.domain.model.KiotvietOrder;

public interface SyncKiotvietOrderUseCase {
    SyncOrderResponse syncAndGetOrderDetails(String orderCode);
    KiotvietOrder syncOrderIfNeeded(KiotvietOrder order);
}