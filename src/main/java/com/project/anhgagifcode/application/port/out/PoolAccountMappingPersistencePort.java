package com.project.anhgagifcode.application.port.out;

import java.util.List;

public interface PoolAccountMappingPersistencePort {
    boolean existsByPoolIdAndAccountId(String poolId, String accountId);
    void saveMapping(String poolId, String accountId);
    void saveMappings(String poolId, List<String> accountIds);
    void removeMappings(String poolId, List<String> accountIds);
}
