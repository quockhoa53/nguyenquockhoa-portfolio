package com.portfolio.infrastructure.scheduler;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.uptime.keep-alive.enabled", havingValue = "true", matchIfMissing = true)
public class UptimeKeepAliveScheduler {

    private static final Logger log = LoggerFactory.getLogger(UptimeKeepAliveScheduler.class);

    private final String chatbotUrl;
    private final String selfUrl;
    private final HttpClient httpClient;

    public UptimeKeepAliveScheduler(
            @Value("${app.chatbot.url:http://localhost:8000}") String chatbotUrl,
            @Value("${app.server.public-url:https://nguyenquockhoa.onrender.com}") String selfUrl) {
        this.chatbotUrl = chatbotUrl.endsWith("/") ? chatbotUrl.substring(0, chatbotUrl.length() - 1) : chatbotUrl;
        this.selfUrl = selfUrl.endsWith("/") ? selfUrl.substring(0, selfUrl.length() - 1) : selfUrl;
        this.httpClient =
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    /**
     * Executes every 10 minutes (600,000 ms) to keep Render Free Tier instances warm 24/7.
     * Initial delay of 1 minute after server boot to allow full initialization.
     */
    @Scheduled(fixedRate = 600_000, initialDelay = 60_000)
    public void pingKeepAlive() {
        log.info("⚡ [Keep-Alive] Starting periodic heartbeat ping for 24/7 high availability...");

        // 1. Ping Chatbot AI Health
        pingEndpoint(chatbotUrl + "/api/health", "AI Chatbot Service");

        // 2. Ping Java Backend Public Endpoint
        if (selfUrl.startsWith("http://") || selfUrl.startsWith("https://")) {
            pingEndpoint(selfUrl + "/api/v1/projects", "Java Backend Service");
        }
    }

    private void pingEndpoint(String url, String serviceName) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", "NQK-Uptime-Heartbeat/1.0")
                    .GET()
                    .build();

            httpClient
                    .sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .thenAccept(response -> {
                        if (response.statusCode() >= 200 && response.statusCode() < 400) {
                            log.debug(
                                    "💚 [Keep-Alive] {} responded with status {}", serviceName, response.statusCode());
                        } else {
                            log.warn("⚠️ [Keep-Alive] {} returned status {}", serviceName, response.statusCode());
                        }
                    })
                    .exceptionally(ex -> {
                        log.warn("⏳ [Keep-Alive] {} heartbeat ping warning: {}", serviceName, ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            log.warn("⏳ [Keep-Alive] Failed to schedule ping for {}: {}", serviceName, e.getMessage());
        }
    }
}
