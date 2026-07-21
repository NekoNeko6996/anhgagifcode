package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.GetKiotvietProductsUseCase;
import com.project.anhgagifcode.application.port.in.dto.GiftPoolDto;
import com.project.anhgagifcode.application.port.in.dto.KiotvietProductDto;
import com.project.anhgagifcode.application.port.in.dto.ProductMappingDto;
import com.project.anhgagifcode.application.port.out.KiotvietProductPersistencePort;
import com.project.anhgagifcode.application.port.out.ProductEggMappingPersistencePort;
import com.project.anhgagifcode.domain.model.KiotvietProduct;
import com.project.anhgagifcode.domain.model.ProductEggMapping;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GetKiotvietProductsService implements GetKiotvietProductsUseCase {

    private final KiotvietProductPersistencePort productPersistencePort;
    private final ProductEggMappingPersistencePort mappingPersistencePort;

    @Override
    public List<KiotvietProductDto> getProducts() {
        List<KiotvietProduct> products = productPersistencePort.findAll();
        List<ProductEggMapping> allMappings = mappingPersistencePort.findAll();

        // Group mappings by kvProductId
        Map<Long, List<ProductEggMapping>> mappingsByProductId = allMappings.stream()
                .filter(m -> m.getProductCode() != null && m.getProductCode().getKvProductId() != null)
                .collect(Collectors.groupingBy(m -> m.getProductCode().getKvProductId()));
      
        return products.stream()
                .map(p -> {
                    List<ProductEggMapping> mappings = mappingsByProductId.getOrDefault(p.getKvProductId(), Collections.emptyList());
                    List<ProductMappingDto> mappingDtos = mappings.stream()
                            .map(m -> {
                                GiftPoolDto poolDto = m.getGiftPoolId() != null ?
                                        GiftPoolDto.builder()
                                                .id(m.getGiftPoolId().getId())
                                                .poolName(m.getGiftPoolId().getPoolName())
                                                .tier(m.getGiftPoolId().getTier())
                                                .createdAt(m.getGiftPoolId().getCreatedAt())
                                                .build() : null;

                                return ProductMappingDto.builder()
                                        .id(m.getId())
                                        .eggTier(m.getEggTier())
                                        .giftPool(poolDto)
                                        .rate(m.getRate())
                                        .mappingsType(m.getMappingsType())
                                        .createdAt(m.getCreatedAt())
                                        .updatedAt(m.getUpdatedAt())
                                        .build();
                            })
                            .collect(Collectors.toList());

                    return KiotvietProductDto.builder()
                            .kvProductId(p.getKvProductId())
                            .code(p.getCode())
                            .name(p.getName())
                            .fullName(p.getFullName())
                            .basePrice(p.getBasePrice())
                            .imageUrl(p.getImageUrl())
                            .lastSyncedAt(p.getLastSyncedAt())
                            .eggType1Qty(p.getEggType1Qty() != null ? p.getEggType1Qty() : 1)
                            .eggType2Qty(p.getEggType2Qty() != null ? p.getEggType2Qty() : 1)
                            .mappings(mappingDtos)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
