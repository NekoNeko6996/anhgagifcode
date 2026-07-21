package com.project.anhgagifcode.application.port.in;

public interface UpdateProductEggQuantitiesUseCase {
    void updateEggQuantities(Long productId, int eggType1Qty, int eggType2Qty);
}
