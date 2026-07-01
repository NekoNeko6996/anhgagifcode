package com.project.anhgagifcode.infrastructure.adapter.out.notification;

import com.project.anhgagifcode.application.port.out.NotificationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class TelegramNotificationAdapter implements NotificationPort {

    @Value("${telegram.bot.token:}")
    private String botToken;

    @Value("${telegram.chat.id:}")
    private String chatId;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void sendAlert(String message) {
        if (botToken == null || botToken.isBlank() || botToken.startsWith("YOUR_")
                || chatId == null || chatId.isBlank() || chatId.startsWith("YOUR_")) {
            log.warn("Telegram Bot Token hoặc Chat ID chưa được cấu hình hoặc vẫn ở dạng mặc định. Bỏ qua thông báo.");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                Map<String, Object> body = new HashMap<>();
                body.put("chat_id", chatId);
                body.put("text", message);
                body.put("parse_mode", "HTML");

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

                restTemplate.postForEntity(url, entity, String.class);
                log.info("Đã gửi cảnh báo Telegram thành công.");
            } catch (Exception e) {
                log.error("Gửi cảnh báo Telegram thất bại: {}", e.getMessage());
            }
        });
    }
}
