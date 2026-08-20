package com.portfolio.infrastructure.web;

import com.portfolio.infrastructure.mail.EmailNotificationService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/mail")
public class AdminMailController {

    private final EmailNotificationService emailService;

    public AdminMailController(EmailNotificationService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/test")
    public ResponseEntity<ApiResponse<Map<String, String>>> testEmail() {
        emailService.sendTestEmail();
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "Đã gửi email test thành công!")));
    }
}
