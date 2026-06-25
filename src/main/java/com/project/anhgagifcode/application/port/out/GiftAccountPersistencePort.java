package com.project.anhgagifcode.application.port.out;

import com.project.anhgagifcode.domain.model.GiftAccount;
import java.util.Optional;

public interface GiftAccountPersistencePort {
    // Đếm số lượng tài khoản còn khả dụng trong 1 Pool
    long countAvailableAccounts(String poolId);
    
    // Lấy 1 tài khoản khả dụng dựa trên Offset random (kèm Pessimistic Lock)
    // Tầng Adapter (Impl) sẽ chịu trách nhiệm chuyển offset này thành Pageable của JPA
    Optional<GiftAccount> loadAvailableAccountWithLock(String poolId, int offset);
    
    // Lưu cập nhật trạng thái tài khoản (chuyển sang ASSIGNED)
    GiftAccount saveAccount(GiftAccount account);
}