package com.project.anhgagifcode.infrastructure.adapter.out.api;

import com.project.anhgagifcode.application.port.out.KiotvietApiPort;
import com.project.anhgagifcode.domain.model.KiotvietOrder;
import com.project.anhgagifcode.domain.model.KiotvietOrderItem;
import com.project.anhgagifcode.domain.model.KiotvietProduct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@RequiredArgsConstructor
public class KiotvietApiAdapter implements KiotvietApiPort {

    @Value("${kiotviet.api.url:https://public.kiotapi.com}")
    private String apiUrl;

    @Value("${kiotviet.api.retailer}")
    private String retailer;

    @Value("${kiotviet.api.client-id}")
    private String clientId;

    @Value("${kiotviet.api.client-secret}")
    private String clientSecret;

    // Tự khởi tạo RestTemplate nếu trong dự án chưa có cấu hình Bean
    private final RestTemplate restTemplate = new RestTemplate();

    @jakarta.annotation.PostConstruct
    public void init() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5 seconds connect timeout
        factory.setReadTimeout(10000);   // 10 seconds read timeout
        this.restTemplate.setRequestFactory(factory);
    }

    // Cache Token để không phải gọi API lấy token liên tục
    private String cachedAccessToken = null;
    private long tokenExpiryTime = 0;

    @Override
    public Optional<KiotvietOrder> fetchOrderFromKiotviet(String orderCode) {
        log.info("Đang gọi API KiotViet để lấy thông tin đơn hàng: {}", orderCode);
        try {
            String token = getValidAccessToken();
            if (token == null) {
                log.error("Không thể lấy Access Token từ KiotViet.");
                return Optional.empty();
            }

            // 1. SỬA ĐƯỜNG DẪN API (Dùng endpoint lấy chi tiết hóa đơn theo Code)
            String url = apiUrl + "/invoices/code/" + orderCode;

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.set("Retailer", retailer);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // 2. SỬA KIỂU PARSE DỮ LIỆU (Parse thẳng ra 1 Object KiotvietInvoice thay vì List)
            ResponseEntity<KiotvietInvoice> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, KiotvietInvoice.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Đã tìm thấy đơn hàng, map dữ liệu sang Domain Model
                return Optional.of(mapToDomain(response.getBody()));
            }

            log.warn("Lấy API thành công nhưng dữ liệu rỗng đối với đơn: {}", orderCode);
            return Optional.empty();

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // 3. BẮT LỖI HTTP TỪ KIOTVIET (Rất quan trọng)
            if (e.getStatusCode().value() == 404) {
                log.warn("Không tìm thấy đơn hàng {} trên KiotViet (KiotViet trả về 404 Not Found).", orderCode);
            } else {
                log.error("Lỗi HTTP từ KiotViet API (Mã {}): {}", e.getStatusCode().value(), e.getResponseBodyAsString());
            }
            return Optional.empty();
        } catch (RestClientException e) {
            log.error("Lỗi hệ thống khi gọi API KiotViet: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<KiotvietProduct> fetchAllProductsFromKiotviet() {
        List<KiotvietProduct> allProducts = new ArrayList<>();
        String token = getValidAccessToken();
        if (token == null) {
            return allProducts;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.set("Retailer", retailer);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        int currentItem = 0;
        int pageSize = 100;
        boolean hasMore = true;

        while (hasMore) {
            String url = apiUrl + "/products?includeInventory=true&isActive=true&includePricebook=true&pageSize=" + pageSize + "&currentItem=" + currentItem;

            // Sử dụng một DTO trung gian để hứng dữ liệu trả về từ API Kiot
            ResponseEntity<KiotvietProductListResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, KiotvietProductListResponse.class);

            if (response.getBody() != null && response.getBody().getData() != null) {
                List<KiotvietProductData> data = response.getBody().getData();

                // Map dữ liệu từ API sang Domain Model
                for (KiotvietProductData item : data) {
                    allProducts.add(KiotvietProduct.builder()
                            .kvProductId(item.getId())
                            .name(item.getName())
                            .fullName(item.getFullName())
                            .basePrice(item.getBasePrice())
                            .imageUrl(item.getImages() != null && !item.getImages().isEmpty() ? item.getImages().get(0) : null)
                            .lastSyncedAt(LocalDateTime.now())
                            .build());
                }

                currentItem += data.size();
                hasMore = (data.size() == pageSize); // Nếu lấy đủ pageSize thì gọi tiếp trang sau
            } else {
                hasMore = false;
            }
        }
        return allProducts;
    }

    /**
     * Logic lấy và quản lý Access Token của KiotViet
     */
    private synchronized String getValidAccessToken() {
        long currentTime = System.currentTimeMillis();
        // Cấp lại token mới nếu token cũ chưa có hoặc sẽ hết hạn trong 5 phút tới (300,000 ms)
        if (cachedAccessToken == null || currentTime >= (tokenExpiryTime - 300000)) {
            log.info("Access Token hết hạn hoặc chưa có, đang lấy mới từ KiotViet...");

            String tokenUrl = "https://id.kiotviet.vn/connect/token"; // URL chuẩn lấy token của KiotViet

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("scopes", "PublicApi.Access");
            map.add("grant_type", "client_credentials");
            map.add("client_id", clientId);
            map.add("client_secret", clientSecret);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            try {
                ResponseEntity<KiotvietTokenResponse> response = restTemplate.postForEntity(
                        tokenUrl, request, KiotvietTokenResponse.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    cachedAccessToken = response.getBody().getAccess_token();
                    // expires_in tính bằng giây, chuyển sang mili giây
                    tokenExpiryTime = currentTime + (response.getBody().getExpires_in() * 1000L);
                    log.info("Lấy Token thành công. Hết hạn sau {} giây.", response.getBody().getExpires_in());
                } else {
                    log.error("Lỗi cấp Token từ KiotViet. HTTP Status: {}", response.getStatusCode());
                }
            } catch (RestClientException e) {
                log.error("Exception khi lấy Token KiotViet: {}", e.getMessage());
            }
        }
        return cachedAccessToken;
    }

    /**
     * Chuyển đổi dữ liệu từ API KiotViet sang Domain Model của hệ thống
     */
    private KiotvietOrder mapToDomain(KiotvietInvoice invoice) {

        // Tùy biến trạng thái giao hàng dựa trên status của KiotViet
        // KiotViet invoiceDelivery Status
        String mappedDeliveryStatus = "Chưa rõ";
        switch (invoice.invoiceDelivery.status) {
            case 1 ->
                mappedDeliveryStatus = "Đang chuẩn bị hàng";
            case 2 ->
                mappedDeliveryStatus = "Đang giao hàng";
            case 3 ->
                mappedDeliveryStatus = "Đã giao hàng";
            case 4 ->
                mappedDeliveryStatus = "Đang chuyển hoàn";
            case 5 ->
                mappedDeliveryStatus = "Đã chuyển hoàn";
            default -> {
            }
        }

        KiotvietOrder order = KiotvietOrder.builder()
                .orderCode(invoice.getCode())
                .customerCode(invoice.getCustomerCode() != null && !invoice.getCustomerCode().trim().isEmpty() ? invoice.getCustomerCode().trim() : "KHACH_LE")
                .deliveryStatus(mappedDeliveryStatus)
                .lastSyncedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        if (invoice.getInvoiceDetails() != null) {
            List<KiotvietOrderItem> items = invoice.getInvoiceDetails().stream()
                    .map(detail -> KiotvietOrderItem.builder()
                    .id(java.util.UUID.randomUUID().toString())
                    .kvProductId(String.valueOf(detail.getProductId()))
                    .quantity((int) detail.getQuantity())
                    .lastSyncedAt(LocalDateTime.now())
                    .build())
                    .collect(Collectors.toList());
            order.setOrderItems(items);
        }

        return order;
    }

    /* =================================================================================
     * CÁC LỚP DTO NỘI BỘ DÙNG ĐỂ MAP JSON TỪ API KIOTVIET (Theo tài liệu Postman)
     * ================================================================================= */
    @Data
    private static class KiotvietTokenResponse {

        private String access_token;
        private int expires_in;
        private String token_type;
    }

    @Data
    private static class KiotvietInvoiceResponse {

        private List<KiotvietInvoice> data;
    }

    @Data
    private static class KiotvietInvoice {

        private long id;
        private String code;
        private String customerCode;
        private String customerName;
        private int status;
        private KiotVietInvoiceDelivery invoiceDelivery;
        private List<KiotvietInvoiceDetail> invoiceDetails;
    }

    @Data
    private static class KiotvietInvoiceDetail {

        private long productId;
        private String productCode;
        private double quantity;
    }

    @Data
    private static class KiotVietInvoiceDelivery {

        private String deliveryCode;
        private int status;
        private String statusValue;
    }
    
    @Data
    public static class KiotvietProductListResponse {
        private int total;
        private int pageSize;
        private List<KiotvietProductData> data;
    }

    @Data
    public static class KiotvietProductData {
        private long id;
        private String name;
        private String fullName;
        private Double basePrice;
        private List<String> images;
    }
}
