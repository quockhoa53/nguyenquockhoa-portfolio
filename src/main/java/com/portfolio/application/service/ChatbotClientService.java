package com.portfolio.application.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

@Service
public class ChatbotClientService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotClientService.class);

    private final RestTemplate restTemplate;
    private final String chatbotUrl;
    private final String internalSecret;

    public ChatbotClientService(
            RestTemplate restTemplate,
            @Value("${app.chatbot.url:http://localhost:8000}") String chatbotUrl,
            @Value("${app.chatbot.internal-secret:}") String internalSecret) {
        this.restTemplate = restTemplate;
        this.chatbotUrl = chatbotUrl.endsWith("/") ? chatbotUrl.substring(0, chatbotUrl.length() - 1) : chatbotUrl;
        this.internalSecret = internalSecret;
    }

    private HttpHeaders createInternalHeaders() {
        return createInternalHeaders(null);
    }

    private HttpHeaders createInternalHeaders(String clientIp) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Internal-API-Key", internalSecret);
        if (clientIp != null && !clientIp.isBlank()) {
            headers.set("X-Forwarded-For", clientIp);
        }
        return headers;
    }

    public void streamChat(String requestJson, String clientIp, OutputStream outputStream) {
        String targetUrl = chatbotUrl + "/api/chat/stream";
        log.info("Proxying SSE Chat stream to AI service: {} for client IP: {}", targetUrl, clientIp);

        try {
            restTemplate.execute(
                    targetUrl,
                    HttpMethod.POST,
                    request -> {
                        request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                        request.getHeaders().set("X-Internal-API-Key", internalSecret);
                        if (clientIp != null && !clientIp.isBlank()) {
                            request.getHeaders().set("X-Forwarded-For", clientIp);
                        }
                        request.getHeaders().set(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE);
                        request.getBody().write(requestJson.getBytes(StandardCharsets.UTF_8));
                    },
                    response -> {
                        if (response.getStatusCode().is4xxClientError()
                                || response.getStatusCode().is5xxServerError()) {
                            String errPayload = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
                            log.warn(
                                    "Downstream AI stream returned error status {}: {}",
                                    response.getStatusCode(),
                                    errPayload);
                            String fallbackEvent =
                                    "data: {\"content\": \"Hệ thống AI đang tiếp nhận nhiều lượt truy cập hoặc đang khởi động. Bạn vui lòng thử lại sau vài giây nhé!\"}\n\ndata: {\"done\": true}\n\n";
                            outputStream.write(fallbackEvent.getBytes(StandardCharsets.UTF_8));
                            outputStream.flush();
                            return null;
                        }

                        try (InputStream is = response.getBody()) {
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = is.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                                outputStream.flush();
                            }
                        }
                        return null;
                    });
        } catch (Exception e) {
            log.error("Error executing Chat stream proxy: {}", e.getMessage());
            try {
                String fallbackEvent =
                        "data: {\"content\": \"Hệ thống AI đang tiếp nhận nhiều lượt truy cập hoặc đang khởi động. Bạn vui lòng thử lại sau vài giây nhé!\"}\n\ndata: {\"done\": true}\n\n";
                outputStream.write(fallbackEvent.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            } catch (Exception writeErr) {
                log.error("Failed to write fallback SSE error: {}", writeErr.getMessage());
            }
        }
    }

    public void streamTts(String requestJson, String clientIp, OutputStream outputStream) {
        String targetUrl = chatbotUrl + "/api/tts";
        log.info("Proxying TTS audio stream to AI service: {} for client IP: {}", targetUrl, clientIp);

        restTemplate.execute(
                targetUrl,
                HttpMethod.POST,
                request -> {
                    request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    request.getHeaders().set("X-Internal-API-Key", internalSecret);
                    if (clientIp != null && !clientIp.isBlank()) {
                        request.getHeaders().set("X-Forwarded-For", clientIp);
                    }
                    request.getHeaders().set(HttpHeaders.ACCEPT, "audio/mpeg, audio/*, */*");
                    request.getBody().write(requestJson.getBytes(StandardCharsets.UTF_8));
                },
                response -> {
                    try (InputStream is = response.getBody()) {
                        StreamUtils.copy(is, outputStream);
                        outputStream.flush();
                    }
                    return null;
                });
    }

    public Object sendFeedback(Object payload, String clientIp) {
        String targetUrl = chatbotUrl + "/api/chat/feedback";
        HttpEntity<Object> entity = new HttpEntity<>(payload, createInternalHeaders(clientIp));
        ResponseEntity<Object> response = restTemplate.exchange(targetUrl, HttpMethod.POST, entity, Object.class);
        return response.getBody();
    }

    public Object getSuggestions() {
        String targetUrl = chatbotUrl + "/api/suggestions";
        HttpEntity<Void> entity = new HttpEntity<>(createInternalHeaders());
        try {
            ResponseEntity<Object> response = restTemplate.exchange(targetUrl, HttpMethod.GET, entity, Object.class);
            return response.getBody();
        } catch (Exception e) {
            log.warn("Failed to fetch suggestions from chatbot service: {}", e.getMessage());
            return Map.of(
                    "suggestions",
                    java.util.List.of(
                            "Kinh nghiệm làm việc & năng lực của Khoa?",
                            "Các dự án nổi bật mà Khoa đã thực hiện?",
                            "Khoa sử dụng những công nghệ Backend & AI nào?",
                            "Làm thế nào để liên hệ và hợp tác với Khoa?"));
        }
    }

    public Object getAdminAiInsights() {
        String targetUrl = chatbotUrl + "/api/admin/ai-insights";
        log.info("Admin fetching AI Insights through Gateway: {}", targetUrl);
        HttpEntity<Void> entity = new HttpEntity<>(createInternalHeaders());
        try {
            ResponseEntity<Object> response = restTemplate.exchange(targetUrl, HttpMethod.GET, entity, Object.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch AI Insights from Chatbot service ({}): {}", targetUrl, e.getMessage());
            return Map.of(
                    "status", "error",
                    "message", "Không thể kết nối tới dịch vụ AI Chatbot: " + e.getMessage(),
                    "data", Map.of(
                            "total_conversations", 0,
                            "total_messages", 0,
                            "positive_ratings", 0,
                            "negative_ratings", 0,
                            "satisfaction_rate", 100,
                            "top_inquiries", java.util.List.of(),
                            "unresolved_queries", java.util.List.of(),
                            "suggested_facts", java.util.List.of()));
        }
    }

    public Object adoptFact(Object payload) {
        String targetUrl = chatbotUrl + "/api/admin/ai-insights/adopt-fact";
        log.info("Admin adopting AI Fact through Gateway: {}", targetUrl);
        HttpEntity<Object> entity = new HttpEntity<>(payload, createInternalHeaders());
        try {
            ResponseEntity<Object> response = restTemplate.exchange(targetUrl, HttpMethod.POST, entity, Object.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to adopt fact on Chatbot service ({}): {}", targetUrl, e.getMessage());
            return Map.of(
                    "status", "error",
                    "message", "Lỗi khi nạp Fact vào bộ nhớ AI: " + e.getMessage());
        }
    }
}
