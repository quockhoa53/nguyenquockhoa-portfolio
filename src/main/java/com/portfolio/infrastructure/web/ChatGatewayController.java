package com.portfolio.infrastructure.web;

import com.portfolio.application.service.ChatbotClientService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatGatewayController {

    private final ChatbotClientService chatbotClientService;

    public ChatGatewayController(ChatbotClientService chatbotClientService) {
        this.chatbotClientService = chatbotClientService;
    }

    @PostMapping(
            value = "/stream",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> streamChat(
            @RequestBody String requestBody, jakarta.servlet.http.HttpServletRequest request) {
        String clientIp = com.portfolio.infrastructure.security.SecurityConfig.clientIp(request);
        StreamingResponseBody responseBody =
                outputStream -> chatbotClientService.streamChat(requestBody, clientIp, outputStream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_EVENT_STREAM_VALUE)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .header(HttpHeaders.CONNECTION, "keep-alive")
                .header("X-Accel-Buffering", "no")
                .body(responseBody);
    }

    @PostMapping(value = "/tts", consumes = MediaType.APPLICATION_JSON_VALUE, produces = "audio/mpeg")
    public ResponseEntity<StreamingResponseBody> streamTts(
            @RequestBody String requestBody, jakarta.servlet.http.HttpServletRequest request) {
        String clientIp = com.portfolio.infrastructure.security.SecurityConfig.clientIp(request);
        StreamingResponseBody responseBody =
                outputStream -> chatbotClientService.streamTts(requestBody, clientIp, outputStream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "audio/mpeg")
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .body(responseBody);
    }

    @PostMapping(
            value = "/feedback",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> submitFeedback(
            @RequestBody Object payload, jakarta.servlet.http.HttpServletRequest request) {
        String clientIp = com.portfolio.infrastructure.security.SecurityConfig.clientIp(request);
        Object result = chatbotClientService.sendFeedback(payload, clientIp);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/suggestions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getSuggestions() {
        Object result = chatbotClientService.getSuggestions();
        return ResponseEntity.ok(result);
    }
}
