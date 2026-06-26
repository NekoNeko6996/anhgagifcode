package com.project.anhgagifcode.infrastructure.config;

import com.project.anhgagifcode.application.port.in.ClaimEggUseCase;
import com.project.anhgagifcode.application.port.in.SyncKiotvietOrderUseCase;
import com.project.anhgagifcode.application.port.out.*;
import com.project.anhgagifcode.application.service.ClaimEggService;
import com.project.anhgagifcode.application.service.SyncKiotvietOrderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public SyncKiotvietOrderUseCase syncKiotvietOrderUseCase(
            KiotvietOrderPersistencePort orderPort,
            KiotvietApiPort apiPort,
            CustomerPersistencePort customerPort,
            ProductEggMappingPersistencePort mappingPort,
            EggPersistencePort eggPort) {
        return new SyncKiotvietOrderService(orderPort, apiPort, customerPort, mappingPort, eggPort);
    }

    @Bean
    public ClaimEggUseCase claimEggUseCase(
            EggPersistencePort eggPort,
            GiftAccountPersistencePort accountPort,
            EggOpeningLogPersistencePort logPort) {
        return new ClaimEggService(eggPort, accountPort, logPort);
    }
}
