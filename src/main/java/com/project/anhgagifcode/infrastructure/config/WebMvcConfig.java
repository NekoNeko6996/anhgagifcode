package com.project.anhgagifcode.infrastructure.config;

import com.project.anhgagifcode.infrastructure.security.RateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Chỉ áp dụng giới hạn 3 lần/phút cho các API lấy/mở trứng
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/eggs/sync", "/api/eggs/claim");
    }
}