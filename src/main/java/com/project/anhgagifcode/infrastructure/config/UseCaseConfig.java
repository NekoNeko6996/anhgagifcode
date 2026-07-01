package com.project.anhgagifcode.infrastructure.config;

import com.project.anhgagifcode.application.port.in.*;
import com.project.anhgagifcode.application.port.out.*;
import com.project.anhgagifcode.application.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class UseCaseConfig {

    private final PlatformTransactionManager transactionManager;

    public UseCaseConfig(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Bean
    public SyncKiotvietOrderUseCase syncKiotvietOrderUseCase(
            KiotvietOrderPersistencePort orderPort,
            KiotvietApiPort apiPort,
            CustomerPersistencePort customerPort,
            ProductEggMappingPersistencePort mappingPort,
            EggPersistencePort eggPort) {
        return new SyncKiotvietOrderService(orderPort, apiPort, customerPort, mappingPort, eggPort, transactionManager);
    }

    @Bean
    public ClaimEggUseCase claimEggUseCase(
            EggPersistencePort eggPort,
            GiftAccountPersistencePort accountPort,
            EggOpeningLogPersistencePort logPort,
            KiotvietOrderPersistencePort orderPort,
            CustomerPersistencePort customerPort,
            SyncKiotvietOrderUseCase syncOrderUseCase) {
        return new ClaimEggService(eggPort, accountPort, logPort, orderPort, customerPort, syncOrderUseCase, transactionManager);
    }
    
    @Bean
    public SyncKiotvietProductUseCase syncKiotvietProductUseCase(
            KiotvietApiPort kiotvietApiPort, 
            KiotvietProductPersistencePort productPersistencePort) {
        return new SyncKiotvietProductService(kiotvietApiPort, productPersistencePort, transactionManager);
    }

    @Bean
    public GetCustomersUseCase getCustomersUseCase(CustomerPersistencePort customerPort) {
        return new GetCustomersService(customerPort);
    }

    @Bean
    public GetGiftAccountsUseCase getGiftAccountsUseCase(GiftAccountPersistencePort accountPort) {
        return new GetGiftAccountsService(accountPort);
    }

    @Bean
    public GetEggsUseCase getEggsUseCase(EggPersistencePort eggPort) {
        return new GetEggsService(eggPort);
    }

    @Bean
    public UpdateEggHatchTimeUseCase updateEggHatchTimeUseCase(EggPersistencePort eggPort) {
        return new UpdateEggHatchTimeService(eggPort);
    }

    @Bean
    public GetGiftPoolsUseCase getGiftPoolsUseCase(GiftPoolPersistencePort poolPort) {
        return new GetGiftPoolsService(poolPort);
    }

    @Bean
    public GetKiotvietOrdersUseCase getKiotvietOrdersUseCase(
            KiotvietOrderPersistencePort orderPort,
            KiotvietProductPersistencePort productPort) {
        return new GetKiotvietOrdersService(orderPort, productPort);
    }

    @Bean
    public GetKiotvietProductsUseCase getKiotvietProductsUseCase(
            KiotvietProductPersistencePort productPort,
            ProductEggMappingPersistencePort mappingPort) {
        return new GetKiotvietProductsService(productPort, mappingPort);
    }

    @Bean
    public CreateGiftPoolUseCase createGiftPoolUseCase(GiftPoolPersistencePort poolPort) {
        return new CreateGiftPoolService(poolPort);
    }

    @Bean
    public RemoveGiftPoolUseCase removeGiftPoolUseCase(GiftPoolPersistencePort poolPort) {
        return new RemoveGiftPoolService(poolPort);
    }

    @Bean
    public AddAccountToPoolUseCase addAccountToPoolUseCase(PoolAccountMappingPersistencePort mappingPort) {
        return new AddAccountToPoolService(mappingPort);
    }

    @Bean
    public AddAccountsToPoolUseCase addAccountsToPoolUseCase(PoolAccountMappingPersistencePort mappingPort) {
        return new AddAccountsToPoolService(mappingPort);
    }

    @Bean
    public GetGiftPoolDetailUseCase getGiftPoolDetailUseCase(
            GiftPoolPersistencePort poolPort,
            GiftAccountPersistencePort accountPort) {
        return new GetGiftPoolDetailService(poolPort, accountPort);
    }

    @Bean
    public RemoveAccountsFromPoolUseCase removeAccountsFromPoolUseCase(PoolAccountMappingPersistencePort mappingPort) {
        return new RemoveAccountsFromPoolService(mappingPort);
    }

    @Bean
    public UpdateGiftPoolUseCase updateGiftPoolUseCase(GiftPoolPersistencePort poolPort) {
        return new UpdateGiftPoolService(poolPort);
    }

    @Bean
    public DeleteGiftAccountsUseCase deleteGiftAccountsUseCase(GiftAccountPersistencePort accountPort) {
        return new DeleteGiftAccountsService(accountPort);
    }

    @Bean
    public LinkProductToEggUseCase linkProductToEggUseCase(ProductEggMappingPersistencePort mappingPort) {
        return new LinkProductToEggService(mappingPort);
    }

    @Bean
    public UpdateMappingRatesUseCase updateMappingRatesUseCase(ProductEggMappingPersistencePort mappingPort) {
        return new UpdateMappingRatesService(mappingPort);
    }

    @Bean
    public DeleteProductEggMappingUseCase deleteProductEggMappingUseCase(ProductEggMappingPersistencePort mappingPort) {
        return new DeleteProductEggMappingService(mappingPort);
    }

    @Bean
    public UpdateCustomerStatusUseCase updateCustomerStatusUseCase(CustomerPersistencePort customerPort) {
        return new UpdateCustomerStatusService(customerPort);
    }

    @Bean
    public UpdateGiftAccountUseCase updateGiftAccountUseCase(GiftAccountPersistencePort accountPort) {
        return new UpdateGiftAccountService(accountPort);
    }

    @Bean
    public UpdateAdminCredentialsUseCase updateAdminCredentialsUseCase(
            com.project.anhgagifcode.infrastructure.adapter.out.persistence.repository.AdminJpaRepository adminJpaRepository,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        return new UpdateAdminCredentialsService(adminJpaRepository, passwordEncoder);
    }
}
