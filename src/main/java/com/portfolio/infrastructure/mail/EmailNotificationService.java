package com.portfolio.infrastructure.mail;

import com.portfolio.domain.model.ContactMessage;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss (XXX)");

    private final JavaMailSender mailSender;
    private final String recipientEmail;
    private final String senderEmail;
    private final boolean mailEnabled;

    public EmailNotificationService(
            JavaMailSender mailSender,
            @Value("${app.mail.recipient:nguyenquockhoa5549@gmail.com}") String recipientEmail,
            @Value("${spring.mail.username:nguyenquockhoa5549@gmail.com}") String senderEmail,
            @Value("${app.mail.enabled:true}") boolean mailEnabled) {
        this.mailSender = mailSender;
        this.recipientEmail = recipientEmail;
        this.senderEmail = senderEmail;
        this.mailEnabled = mailEnabled;
    }

    @Async
    public void sendContactNotification(ContactMessage message) {
        if (!mailEnabled || mailSender == null) {
            log.info("Email notification is disabled or mail sender not configured.");
            return;
        }

        try {
            log.info("Sending contact notification email to {} for message from {}", recipientEmail, message.email());
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            helper.setFrom(senderEmail, "NQK Portfolio Notification");
            helper.setTo(recipientEmail);
            helper.setReplyTo(message.email(), message.name());
            helper.setSubject("📬 [NQK Portfolio] Tin nhắn mới từ " + message.name() + ": " + message.subject());

            String timeStr = message.createdAt() != null ? message.createdAt().format(FORMATTER) : "Vừa xong";
            String safeContent = escapeHtml(message.message()).replace("\n", "<br/>");
            String safeName = escapeHtml(message.name());
            String safeEmail = escapeHtml(message.email());
            String safeSubject = escapeHtml(message.subject());

            String htmlBody =
                    """
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

            helper.setText(htmlBody, true);
            mailSender.send(mimeMessage);
            log.info("Successfully delivered contact notification email to {}", recipientEmail);

        } catch (Exception e) {
            log.error("Failed to send contact notification email to {}: {}", recipientEmail, e.getMessage(), e);
        }
    }

    public void sendTestEmail() {
        try {
            log.info("Sending TEST email to {}", recipientEmail);
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setFrom(senderEmail, "NQK Portfolio System");
            helper.setTo(recipientEmail);
            helper.setSubject("🧪 [Test Email] Kiểm tra kết nối Gmail SMTP thành công!");
            helper.setText(
                    "<h3>Xin chào Nguyễn Quốc Khoa!</h3><p>Hệ thống Portfolio Backend đã kết nối thành công tới Gmail SMTP và sẵn sàng gửi thông báo khi có người liên hệ.</p>",
                    true);
            mailSender.send(mimeMessage);
            log.info("Test email delivered successfully to {}", recipientEmail);
        } catch (Exception e) {
            log.error("Test email failed to send: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi gửi test email: " + e.getMessage(), e);
        }
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
