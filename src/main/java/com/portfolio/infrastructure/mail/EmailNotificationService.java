package com.portfolio.infrastructure.mail;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.domain.model.ContactMessage;
import jakarta.mail.internet.MimeMessage;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss (XXX)");

    private final JavaMailSender defaultMailSender;
    private final String recipientEmail;
    private final String senderEmail;
    private final String password;
    private final String resendApiKey;
    private final boolean mailEnabled;
    private final ObjectMapper objectMapper;

    private final AtomicReference<String> lastStatus =
            new AtomicReference<>("Chưa có lượt gửi email nào kể từ khi khởi động.");

    public EmailNotificationService(
            JavaMailSender defaultMailSender,
            @Value("${app.mail.recipient:nguyenquockhoa5549@gmail.com}") String recipientEmail,
            @Value("${spring.mail.username:nguyenquockhoa5549@gmail.com}") String senderEmail,
            @Value("${spring.mail.password:}") String password,
            @Value("${app.mail.resend-api-key:}") String resendApiKey,
            @Value("${app.mail.enabled:true}") boolean mailEnabled,
            ObjectMapper objectMapper) {
        this.defaultMailSender = defaultMailSender;
        this.recipientEmail = recipientEmail;
        this.senderEmail = senderEmail;
        this.password = password;
        this.resendApiKey = resendApiKey;
        this.mailEnabled = mailEnabled;
        this.objectMapper = objectMapper;
    }

    public String getLastStatus() {
        return lastStatus.get();
    }

    @Async
    public void sendContactNotification(ContactMessage message) {
        if (!mailEnabled) {
            String status = "Email notification is disabled in configuration (app.mail.enabled=false).";
            log.warn(status);
            lastStatus.set(status);
            return;
        }

        try {
            log.info("Sending contact notification email to {} for message from {}", recipientEmail, message.email());
            deliverMessage(message.name(), message.email(), message.subject(), message.message(), message.createdAt());
            String successMsg = "✅ Đã gửi email thông báo thành công tới " + recipientEmail + " lúc "
                    + OffsetDateTime.now().format(FORMATTER);
            log.info(successMsg);
            lastStatus.set(successMsg);
        } catch (Exception e) {
            String errorMsg = "❌ Lỗi gửi email thông báo tới " + recipientEmail + ": " + e.getMessage();
            log.error(errorMsg, e);
            lastStatus.set(errorMsg);
        }
    }

    public String sendTestEmail() {
        try {
            log.info("Triggering manual TEST email to {}", recipientEmail);
            deliverMessage(
                    "Hệ Thống Portfolio Test",
                    senderEmail,
                    "Kiểm tra kết nối Gmail SMTP / Mail Service",
                    "Đây là email kiểm tra kết nối từ hệ thống Portfolio của Nguyễn Quốc Khoa. Dịch vụ gửi email thông báo đã hoạt động hoàn hảo!",
                    OffsetDateTime.now());
            String success = "✅ Đã gửi TEST email thành công tới " + recipientEmail + " lúc "
                    + OffsetDateTime.now().format(FORMATTER);
            log.info(success);
            lastStatus.set(success);
            return success;
        } catch (Exception e) {
            String error = "❌ Gửi test email thất bại: " + e.getMessage();
            log.error(error, e);
            lastStatus.set(error);
            throw new RuntimeException(error, e);
        }
    }

    private void deliverMessage(
            String fromName, String replyToEmail, String subject, String content, OffsetDateTime createdAt)
            throws Exception {
        String timeStr = createdAt != null
                ? createdAt.format(FORMATTER)
                : OffsetDateTime.now().format(FORMATTER);
        String safeContent = escapeHtml(content).replace("\n", "<br/>");
        String safeName = escapeHtml(fromName);
        String safeEmail = escapeHtml(replyToEmail);
        String safeSubject = escapeHtml(subject);

        String htmlBody = buildHtmlTemplate(safeName, safeEmail, safeSubject, timeStr, safeContent);

        // 1. First priority: Resend HTTPS API if API key provided (immune to Render SMTP port blocks)
        if (resendApiKey != null && !resendApiKey.isBlank()) {
            if (sendViaResendApi(fromName, replyToEmail, subject, htmlBody)) {
                return;
            }
        }

        // 2. Second priority: Port 465 Direct SSL SMTPS (Default mailSender configured for 465)
        try {
            MimeMessage mimeMessage = defaultMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setFrom(senderEmail, "NQK Portfolio Notification");
            helper.setTo(recipientEmail);
            if (replyToEmail != null && replyToEmail.contains("@")) {
                helper.setReplyTo(replyToEmail, fromName);
            }
            helper.setSubject("📬 [NQK Portfolio] Tin nhắn mới từ " + fromName + ": " + subject);
            helper.setText(htmlBody, true);

            defaultMailSender.send(mimeMessage);
            log.info("Delivered email successfully via Port 465 SSL");
            return;
        } catch (Exception e465) {
            log.warn(
                    "Port 465 SSL delivery failed ({}), attempting fallback to Port 587 STARTTLS...",
                    e465.getMessage());
        }

        // 3. Fallback: Port 587 STARTTLS
        JavaMailSenderImpl fallback587 = createFallback587Sender();
        MimeMessage mimeMessage587 = fallback587.createMimeMessage();
        MimeMessageHelper helper587 = new MimeMessageHelper(mimeMessage587, "UTF-8");
        helper587.setFrom(senderEmail, "NQK Portfolio Notification");
        helper587.setTo(recipientEmail);
        if (replyToEmail != null && replyToEmail.contains("@")) {
            helper587.setReplyTo(replyToEmail, fromName);
        }
        helper587.setSubject("📬 [NQK Portfolio] Tin nhắn mới từ " + fromName + ": " + subject);
        helper587.setText(htmlBody, true);

        fallback587.send(mimeMessage587);
        log.info("Delivered email successfully via Port 587 STARTTLS fallback");
    }

    private boolean sendViaResendApi(String fromName, String replyToEmail, String subject, String htmlContent) {
        try {
            log.info("Attempting email dispatch via Resend HTTPS API...");
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            String payload = objectMapper.writeValueAsString(Map.of(
                    "from",
                    "NQK Portfolio <onboarding@resend.dev>",
                    "to",
                    List.of(recipientEmail),
                    "reply_to",
                    replyToEmail != null && replyToEmail.contains("@") ? replyToEmail : recipientEmail,
                    "subject",
                    "📬 [NQK Portfolio] Tin nhắn mới từ " + fromName + ": " + subject,
                    "html",
                    htmlContent));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + resendApiKey.trim())
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(12))
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Successfully delivered email via Resend HTTPS API: {}", response.body());
                return true;
            } else {
                log.warn("Resend API rejected with status {}: {}", response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            log.warn("Resend API dispatch failed: {}", e.getMessage());
            return false;
        }
    }

    private JavaMailSenderImpl createFallback587Sender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost("smtp.gmail.com");
        sender.setPort(587);
        sender.setUsername(senderEmail);
        sender.setPassword(password);
        sender.setDefaultEncoding("UTF-8");

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.trust", "*");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        return sender;
    }

    private String buildHtmlTemplate(
            String safeName, String safeEmail, String safeSubject, String timeStr, String safeContent) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #0f172a; color: #f8fafc; margin: 0; padding: 24px; }
                        .container { max-width: 600px; margin: 0 auto; background: #1e293b; border-radius: 16px; border: 1px solid #334155; overflow: hidden; box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.5); }
                        .header { background: linear-gradient(135deg, #4f46e5, #06b6d4); padding: 28px 24px; text-align: center; }
                        .header h1 { margin: 0; color: #ffffff; font-size: 20px; font-weight: 700; letter-spacing: -0.5px; }
                        .header p { margin: 6px 0 0; color: #e0e7ff; font-size: 14px; }
                        .content { padding: 24px; }
                        .meta-box { background: #0f172a; border-radius: 12px; padding: 16px; border: 1px solid #334155; margin-bottom: 20px; }
                        .meta-item { display: flex; margin-bottom: 10px; font-size: 14px; }
                        .meta-item:last-child { margin-bottom: 0; }
                        .meta-label { width: 110px; color: #94a3b8; font-weight: 600; flex-shrink: 0; }
                        .meta-value { color: #f1f5f9; word-break: break-all; }
                        .meta-value a { color: #38bdf8; text-decoration: none; font-weight: 500; }
                        .message-box { background: #0f172a; border-left: 4px solid #6366f1; border-radius: 0 12px 12px 0; padding: 18px; color: #f8fafc; font-size: 15px; line-height: 1.6; margin-bottom: 24px; }
                        .btn-container { text-align: center; margin: 28px 0 12px; }
                        .btn { display: inline-block; background: linear-gradient(135deg, #4f46e5, #6366f1); color: #ffffff !important; padding: 12px 28px; border-radius: 8px; font-weight: 600; text-decoration: none; font-size: 14px; box-shadow: 0 4px 12px rgba(79, 70, 229, 0.4); }
                        .footer { padding: 16px 24px; text-align: center; font-size: 12px; color: #64748b; border-top: 1px solid #334155; background: #0f172a; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>📬 Có tin nhắn liên hệ mới!</h1>
                            <p>Từ khách truy cập website Nguyen Quoc Khoa Portfolio</p>
                        </div>
                        <div class="content">
                            <div class="meta-box">
                                <div class="meta-item">
                                    <span class="meta-label">👤 Người gửi:</span>
                                    <span class="meta-value"><strong>%s</strong></span>
                                </div>
                                <div class="meta-item">
                                    <span class="meta-label">✉️ Email:</span>
                                    <span class="meta-value"><a href="mailto:%s">%s</a></span>
                                </div>
                                <div class="meta-item">
                                    <span class="meta-label">📌 Chủ đề:</span>
                                    <span class="meta-value">%s</span>
                                </div>
                                <div class="meta-item">
                                    <span class="meta-label">⏰ Thời gian:</span>
                                    <span class="meta-value">%s</span>
                                </div>
                            </div>
                            <h3 style="color: #cbd5e1; font-size: 14px; margin: 0 0 8px 4px; text-transform: uppercase; letter-spacing: 0.5px;">Nội dung tin nhắn:</h3>
                            <div class="message-box">
                                %s
                            </div>
                            <div class="btn-container">
                                <a class="btn" href="mailto:%s?subject=Re: %s">✉️ Trả lời ngay cho %s</a>
                            </div>
                        </div>
                        <div class="footer">
                            Email này được gửi tự động từ hệ thống Portfolio Backend của Nguyễn Quốc Khoa.
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(
                        safeName,
                        safeEmail,
                        safeEmail,
                        safeSubject,
                        timeStr,
                        safeContent,
                        safeEmail,
                        safeSubject,
                        safeName);
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
