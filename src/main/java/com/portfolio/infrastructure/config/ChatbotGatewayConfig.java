package com.portfolio.infrastructure.config;

import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ChatbotGatewayConfig {

    @Bean
    public RestTemplate chatbotRestTemplate(RestTemplateBuilder builder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(120000); // 2 minutes read timeout for long LLM token streaming
        return builder.requestFactory(() -> factory)
                .setConnectTimeout(Duration.ofSeconds(15))
                .setReadTimeout(Duration.ofMinutes(2))
                .build();
    }
}
