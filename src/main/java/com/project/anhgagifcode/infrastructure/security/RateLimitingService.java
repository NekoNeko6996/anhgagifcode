package com.project.anhgagifcode.infrastructure.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitingService {

    private final Cache<String, Bucket> cache = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES) // Tự động xóa sau 10 phút không hoạt động
            .maximumSize(100000) // Giới hạn tối đa 100,000 bản ghi để tránh cạn kiệt RAM
            .build();

    public Bucket resolveBucket(String ip) {
        return cache.get(ip, this::newBucket);
    }

    private Bucket newBucket(String ip) {
        // Cấu hình: Tối đa 3 request, nạp lại 3 token mỗi 1 phút
        Refill refill = Refill.intervally(3, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(3, refill);
        return Bucket.builder().addLimit(limit).build();
    }
}