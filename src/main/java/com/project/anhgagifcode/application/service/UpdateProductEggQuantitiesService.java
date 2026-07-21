package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.UpdateProductEggQuantitiesUseCase;
import com.project.anhgagifcode.application.port.out.KiotvietProductPersistencePort;
import com.project.anhgagifcode.domain.exception.ResourceNotFoundException;
import com.project.anhgagifcode.domain.model.KiotvietProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class UpdateProductEggQuantitiesService implements UpdateProductEggQuantitiesUseCase {

    private final KiotvietProductPersistencePort productPersistencePort;

    @Override
    @Transactional
    public void updateEggQuantities(Long productId, int eggType1Qty, int eggType2Qty) {
        KiotvietProduct product = productPersistencePort.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm này."));

        product.setEggType1Qty(eggType1Qty);
        product.setEggType2Qty(eggType2Qty);

        productPersistencePort.saveProduct(product);
    }
}
