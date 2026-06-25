package com.project.anhgagifcode.infrastructure.config;

import com.project.anhgagifcode.application.port.in.SyncSapoOrderUseCase;
import com.project.anhgagifcode.application.port.out.EggPersistencePort;
import com.project.anhgagifcode.application.port.out.ProductEggMappingPersistencePort;
import com.project.anhgagifcode.application.port.out.SapoOrderPersistencePort;
import com.project.anhgagifcode.application.service.SyncSapoOrderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public SyncSapoOrderUseCase syncSapoOrderUseCase(
            SapoOrderPersistencePort orderPersistencePort,
            EggPersistencePort eggPersistencePort,
            ProductEggMappingPersistencePort mappingPersistencePort) {
        
        return new SyncSapoOrderService(orderPersistencePort, eggPersistencePort, mappingPersistencePort);
    }
}