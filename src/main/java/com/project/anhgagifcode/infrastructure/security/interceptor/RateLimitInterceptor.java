package com.project.anhgagifcode.infrastructure.security.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    // Lưu lịch sử request theo dạng: Key="IP-URI", Value="Danh sách thời gian gọi"
    private final Map<String, Deque<Long>> requestCounts = new ConcurrentHashMap<>();
    
    private static final int MAX_REQUESTS = 3; // Tối đa 3 lần
    private static final long TIME_WINDOW_MS = 60000; // Trong vòng 1 phút (60,000 ms)

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Lấy IP thật của người dùng (Xuyên qua Cloudflare/Nginx nếu có)
        String clientIp = getClientIp(request);
        String requestPath = request.getRequestURI();
        
        // Chỉ giới hạn các API liên quan đến trứng
        if (!requestPath.startsWith("/api/eggs")) {
            return true;
        }

        String key = clientIp + "-" + requestPath;

        synchronized (key.intern()) {
            Deque<Long> timestamps = requestCounts.computeIfAbsent(key, k -> new LinkedList<>());
            long now = System.currentTimeMillis();

            // Xóa các mốc thời gian đã quá 1 phút
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > TIME_WINDOW_MS) {
                timestamps.pollFirst();
            }

            // Nếu vẫn còn >= 3 mốc thời gian -> Bị Ban tạm thời
            if (timestamps.size() >= MAX_REQUESTS) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // HTTP 429
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\": \"Too Many Requests\", \"message\": \"Bạn thao tác quá nhanh. Vui lòng chờ 1 phút trước khi thử lại!\"}");
                return false;
            }

            // Hợp lệ -> Thêm thời gian hiện tại vào hàng đợi
            timestamps.addLast(now);
        }
        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}