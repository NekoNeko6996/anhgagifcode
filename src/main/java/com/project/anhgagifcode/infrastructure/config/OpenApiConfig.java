package com.project.anhgagifcode.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Hệ Thống Quà Tặng & Mở Trứng API")
                        .version("1.0.0")
                        .description("Tài liệu API cho dự án nhận Webhook từ Sapo và quản lý Giftcode.")
                        .contact(new Contact().name("Zader Team")));
    }
}