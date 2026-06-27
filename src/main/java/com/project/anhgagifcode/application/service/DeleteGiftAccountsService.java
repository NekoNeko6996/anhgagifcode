package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.DeleteGiftAccountsUseCase;
import com.project.anhgagifcode.application.port.in.dto.DeleteGiftAccountsRequest;
import com.project.anhgagifcode.application.port.out.GiftAccountPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class DeleteGiftAccountsService implements DeleteGiftAccountsUseCase {

    private final GiftAccountPersistencePort giftAccountPersistencePort;

    @Override
    @Transactional
    public void deleteAccounts(DeleteGiftAccountsRequest request) {
        giftAccountPersistencePort.deleteAccounts(request.getAccountIds());
    }
}
