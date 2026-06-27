package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.SyncKiotvietProductUseCase;
import com.project.anhgagifcode.application.port.out.KiotvietApiPort;
import com.project.anhgagifcode.application.port.out.KiotvietProductPersistencePort;
import com.project.anhgagifcode.domain.model.KiotvietProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncKiotvietProductService implements SyncKiotvietProductUseCase {

    private final KiotvietApiPort kiotvietApiPort;
    private final KiotvietProductPersistencePort productPersistencePort;

    @Transactional
    @Override
    public int syncProductsFromKiotviet() {
        List<KiotvietProduct> productsFromApi = kiotvietApiPort.fetchAllProductsFromKiotviet();
        int savedCount = 0;

        for (KiotvietProduct p : productsFromApi) {
            productPersistencePort.saveProduct(p);
            savedCount++;
        }
        
        log.info("Đồng bộ hoàn tất {} sản phẩm vào cơ sở dữ liệu.", savedCount);
        return savedCount;
    }
}