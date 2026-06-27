package com.project.anhgagifcode.application.port.out;

import com.project.anhgagifcode.domain.model.GiftAccount;
import java.util.List;
import java.util.Optional;

public interface GiftAccountPersistencePort {

    // Hàm quan trọng nhất: Đếm số quà còn lại trong 1 Pool
    long countAvailableAccountsByPoolId(String poolId);

    // Dùng OFFSET để pick ngẫu nhiên 1 Account và LOCK nó lại (Pessimistic Lock)
    Optional<GiftAccount> pickRandomAvailableAccountForUpdate(String poolId, int offset);

    // Cập nhật trạng thái Account sau khi đã trao cho khách
    void updateAccount(GiftAccount account);

    GiftAccount save(GiftAccount account);

    void saveAll(List<GiftAccount> accounts);

    List<GiftAccount> findAll();

    List<GiftAccount> findAccountsByPoolId(String poolId);

    void deleteAccounts(List<String> ids);

    Optional<GiftAccount> findById(String id);
}
