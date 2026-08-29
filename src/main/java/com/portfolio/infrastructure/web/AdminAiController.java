package com.portfolio.infrastructure.web;

import com.portfolio.application.service.ChatbotClientService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminAiController {

    private final ChatbotClientService chatbotClientService;

    public AdminAiController(ChatbotClientService chatbotClientService) {
        this.chatbotClientService = chatbotClientService;
    }

    @GetMapping(value = "/ai-insights", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getAdminAiInsights() {
        Object insights = chatbotClientService.getAdminAiInsights();
        return ResponseEntity.ok(insights);
    }

    @PostMapping(
            value = "/ai-insights/adopt-fact",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> adoptSuggestedFact(@RequestBody Object payload) {
        Object result = chatbotClientService.adoptFact(payload);
        return ResponseEntity.ok(result);
    }
}
