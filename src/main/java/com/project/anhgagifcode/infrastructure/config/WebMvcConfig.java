package com.project.anhgagifcode.infrastructure.config;

import com.project.anhgagifcode.infrastructure.security.interceptor.RateLimitInterceptor;
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
        // Kích hoạt chặn Spam cho toàn bộ API người dùng tra cứu
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/eggs/**");
    }
}