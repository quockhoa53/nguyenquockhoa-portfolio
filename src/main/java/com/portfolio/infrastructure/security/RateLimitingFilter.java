package com.portfolio.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitingFilter extends OncePerRequestFilter {

    // Login endpoints: Strict limit to prevent Brute-Force & CPU exhaustion (BCrypt DoS)
    private static final int AUTH_MAX_REQUESTS = 6;
    private static final long AUTH_WINDOW_MS = 30_000; // 30 seconds

    // Admin endpoints: Moderate limit for authenticated operations
    private static final int ADMIN_MAX_REQUESTS = 120;
    private static final long ADMIN_WINDOW_MS = 60_000; // 60 seconds

    // General API endpoints: High throughput limit for regular visitors
    private static final int GENERAL_MAX_REQUESTS = 180;
    private static final long GENERAL_WINDOW_MS = 60_000; // 60 seconds

    private final Map<String, RequestBucket> authBuckets = new ConcurrentHashMap<>();
    private final Map<String, RequestBucket> adminBuckets = new ConcurrentHashMap<>();
    private final Map<String, RequestBucket> generalBuckets = new ConcurrentHashMap<>();

    private volatile long lastCleanup = System.currentTimeMillis();
    private static final long CLEANUP_INTERVAL_MS = 300_000; // Clean every 5 minutes

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Periodic eviction of stale IP buckets to prevent memory accumulation
        evictStaleBucketsIfNeeded();

        String path = request.getRequestURI();
        String clientIp = SecurityConfig.clientIp(request);

        // 1. Strict protection for Authentication endpoints (Chống Brute Force & DoS CPU)
        if (path.contains("/admin/auth/login") || path.contains("/admin/auth/verify-2fa")) {
            if (!isAllowed(authBuckets, clientIp, AUTH_MAX_REQUESTS, AUTH_WINDOW_MS)) {
                sendRateLimitError(
                        response, "Hệ thống phát hiện tần suất gửi yêu cầu quá nhanh. Vui lòng thử lại sau 30 giây.");
                return;
            }
        }
        // 2. Admin APIs protection
        else if (path.startsWith("/api/v1/admin")) {
            if (!isAllowed(adminBuckets, clientIp, ADMIN_MAX_REQUESTS, ADMIN_WINDOW_MS)) {
                sendRateLimitError(response, "Quá nhiều yêu cầu tới trang quản trị. Vui lòng giảm tần suất thao tác.");
                return;
            }
        }
        // 3. General Public APIs protection
        else if (path.startsWith("/api/v1")) {
            if (!isAllowed(generalBuckets, clientIp, GENERAL_MAX_REQUESTS, GENERAL_WINDOW_MS)) {
                sendRateLimitError(response, "Quá giới hạn tần suất truy cập API. Vui lòng thử lại sau giây lát.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAllowed(Map<String, RequestBucket> map, String key, int maxRequests, long windowMs) {
        long now = System.currentTimeMillis();
        var bucket = map.compute(key, (k, existing) -> {
            if (existing == null || (now - existing.startTime) > windowMs) {
                return new RequestBucket(now, new AtomicInteger(1));
            }
            existing.count.incrementAndGet();
            return existing;
        });
        return bucket.count.get() <= maxRequests;
    }

    private void sendRateLimitError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Retry-After", "30");
        response.getWriter()
                .write(String.format(
                        "{\"success\":false,\"error\":\"TOO_MANY_REQUESTS\",\"message\":\"%s\"}", message));
    }

    private void evictStaleBucketsIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastCleanup > CLEANUP_INTERVAL_MS) {
            lastCleanup = now;
            authBuckets.entrySet().removeIf(e -> now - e.getValue().startTime > AUTH_WINDOW_MS * 2);
            adminBuckets.entrySet().removeIf(e -> now - e.getValue().startTime > ADMIN_WINDOW_MS * 2);
            generalBuckets.entrySet().removeIf(e -> now - e.getValue().startTime > GENERAL_WINDOW_MS * 2);
        }
    }

    private static class RequestBucket {
        final long startTime;
        final AtomicInteger count;

        RequestBucket(long startTime, AtomicInteger count) {
            this.startTime = startTime;
            this.count = count;
        }
    }
}
