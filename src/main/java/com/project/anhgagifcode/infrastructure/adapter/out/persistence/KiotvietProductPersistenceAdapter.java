package com.project.anhgagifcode.infrastructure.adapter.out.persistence;

import com.project.anhgagifcode.application.port.out.KiotvietProductPersistencePort;
import com.project.anhgagifcode.domain.model.KiotvietProduct;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.entity.KiotvietProducts;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.mapper.KiotvietProductMapper;
import com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository.KiotvietProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class KiotvietProductPersistenceAdapter implements KiotvietProductPersistencePort {

    private final KiotvietProductJpaRepository repository;
    private final KiotvietProductMapper mapper;

    @Override
    public Optional<KiotvietProduct> findById(long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public KiotvietProduct saveProduct(KiotvietProduct product) {
        Optional<KiotvietProducts> existingOpt = repository.findById(product.getKvProductId());
        KiotvietProducts entity = mapper.toEntity(product);
        if (existingOpt.isPresent()) {
            KiotvietProducts existing = existingOpt.get();
            if (product.getEggType1Qty() == null) {
                entity.setEggType1Qty(existing.getEggType1Qty());
            }
            if (product.getEggType2Qty() == null) {
                entity.setEggType2Qty(existing.getEggType2Qty());
            }
        } else {
            if (entity.getEggType1Qty() == null) {
                entity.setEggType1Qty(1);
            }
            if (entity.getEggType2Qty() == null) {
                entity.setEggType2Qty(1);
            }
        }
        KiotvietProducts savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public List<KiotvietProduct> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
