package com.portfolio.infrastructure.config;

import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

    private static final Logger log = LoggerFactory.getLogger(MailConfig.class);

    @Bean
    public JavaMailSender javaMailSender(
            @Value("${spring.mail.host:smtp.gmail.com}") String host,
            @Value("${spring.mail.port:465}") int port,
            @Value("${spring.mail.username:nguyenquockhoa5549@gmail.com}") String username,
            @Value("${spring.mail.password:}") String password) {

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        mailSender.setDefaultEncoding("UTF-8");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.trust", "*");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        if (port == 465) {
            props.put("mail.transport.protocol", "smtps");
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");
        } else {
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
        }

        if (password == null || password.isBlank()) {
            log.warn(
                    "⚠️ MAIL_PASSWORD is not set in environment! Email notifications will fail until MAIL_PASSWORD is provided.");
        } else {
            log.info(
                    "✅ MailConfig initialized successfully for sender: {} via {}:{} (Protocol: {})",
                    username,
                    host,
                    port,
                    port == 465 ? "SMTPS/SSL" : "SMTP/STARTTLS");
        }

        return mailSender;
    }
}
