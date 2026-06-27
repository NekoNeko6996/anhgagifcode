package com.project.anhgagifcode.application.port.out;

import com.project.anhgagifcode.domain.model.KiotvietProduct;
import java.util.List;
import java.util.Optional;

public interface KiotvietProductPersistencePort {
    Optional<KiotvietProduct> findById(long id);
    KiotvietProduct saveProduct(KiotvietProduct product);
    List<KiotvietProduct> findAll();
}
