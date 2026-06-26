package com.project.anhgagifcode.infrastructure.adapter.out.api;

import com.project.anhgagifcode.application.port.out.KiotvietApiPort;
import com.project.anhgagifcode.domain.model.KiotvietOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component // Annotation cực kỳ quan trọng để Spring nhận diện làm Bean
@RequiredArgsConstructor
public class KiotvietApiAdapter implements KiotvietApiPort {

    // Nếu dùng RestTemplate, bạn sẽ inject nó ở đây
    // private final RestTemplate restTemplate;

    @Override
    public Optional<KiotvietOrder> fetchOrderFromKiotviet(String orderCode) {
        log.info("Đang gọi API KiotViet để lấy thông tin đơn hàng: {}", orderCode);
        
        try {
            // Logic gọi HTTP GET đến KiotViet API tại đây
            System.out.println(orderCode);
            // String url = "https://public.kiotapi.com/invoices?code=" + orderCode;
            // HttpHeaders headers = new HttpHeaders();
            // headers.setBearerAuth("YOUR_KIOTVIET_TOKEN");
            // headers.set("Retailer", "YOUR_RETAILER");
            // HttpEntity<String> entity = new HttpEntity<>(headers);
            // ResponseEntity<KiotvietApiResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, KiotvietApiResponse.class);
            // return Optional.of(mapToDomain(response.getBody()));

            /* =========================================
             * CODE TẠM THỜI ĐỂ BYPASS LỖI KHỞI ĐỘNG/TEST
             * ========================================= */
            log.warn("Chưa cấu hình API KiotViet thật. Trả về Optional.empty() cho mã đơn: {}", orderCode);
            return Optional.empty();

        } catch (Exception e) {
            log.error("Lỗi khi gọi API KiotViet: {}", e.getMessage());
            return Optional.empty();
        }
    }
}