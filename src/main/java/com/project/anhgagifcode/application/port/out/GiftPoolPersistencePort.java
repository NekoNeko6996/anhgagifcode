package com.project.anhgagifcode.application.port.out;

import com.project.anhgagifcode.domain.model.GiftPool;
import java.util.List;
import java.util.Optional;

public interface GiftPoolPersistencePort {
    List<GiftPool> findAll();
    Optional<GiftPool> findById(String id);
    GiftPool savePool(GiftPool pool);
    void deletePool(String id);
    boolean existsById(String id);
    boolean hasAssociatedEggs(String id);
}
