package com.project.anhgagifcode.application.port.in;

import com.project.anhgagifcode.domain.model.SapoOrder;

public interface SyncSapoOrderUseCase {
    
    /**
     * Xử lý payload webhook từ Sapo
     * @param incomingOrder Đối tượng Domain chứa dữ liệu đã được map từ JSON Webhook
     */
    void syncOrder(SapoOrder incomingOrder);
}