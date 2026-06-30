package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.SyncKiotvietProductUseCase;
import com.project.anhgagifcode.application.port.out.KiotvietApiPort;
import com.project.anhgagifcode.application.port.out.KiotvietProductPersistencePort;
import com.project.anhgagifcode.domain.model.KiotvietProduct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Slf4j
public class SyncKiotvietProductService implements SyncKiotvietProductUseCase {

    private final KiotvietApiPort kiotvietApiPort;
    private final KiotvietProductPersistencePort productPersistencePort;
    private final TransactionTemplate transactionTemplate;

    public SyncKiotvietProductService(
            KiotvietApiPort kiotvietApiPort,
            KiotvietProductPersistencePort productPersistencePort,
            PlatformTransactionManager transactionManager) {
        this.kiotvietApiPort = kiotvietApiPort;
        this.productPersistencePort = productPersistencePort;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public int syncProductsFromKiotviet() {
        // 1. Gọi API ngoài Transaction để tránh tắc nghẽn connection pool
        List<KiotvietProduct> productsFromApi = kiotvietApiPort.fetchAllProductsFromKiotviet();
        
        // 2. Chạy lưu sản phẩm trong Transaction
        return transactionTemplate.execute(status -> {
            int savedCount = 0;
            for (KiotvietProduct p : productsFromApi) {
                productPersistencePort.saveProduct(p);
                savedCount++;
            }
            log.info("Đồng bộ hoàn tất {} sản phẩm vào cơ sở dữ liệu.", savedCount);
            return savedCount;
        });
    }
}