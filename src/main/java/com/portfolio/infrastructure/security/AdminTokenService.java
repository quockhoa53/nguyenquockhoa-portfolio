package com.portfolio.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AdminTokenService {

    private final String secret;

    public AdminTokenService(
            @Value("${app.guest.cookie-secret:portfolio-default-token-secret-key-2024}") String secret) {
        this.secret = secret;
    }

    public String generateToken(String username) {
        long expiry = Instant.now().plusSeconds(86400 * 7).getEpochSecond(); // 7 days validity
        String payload = username + ":" + expiry;
        String signature = sign(payload);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString((payload + ":" + signature).getBytes(StandardCharsets.UTF_8));
    }

    public String generatePreAuthToken(String username) {
        long expiry = Instant.now().plusSeconds(300).getEpochSecond(); // 5 minutes validity
        String payload = "PRE_AUTH:" + username + ":" + expiry;
        String signature = sign(payload);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString((payload + ":" + signature).getBytes(StandardCharsets.UTF_8));
    }

    public String validateToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            String cleanToken = token.startsWith("Bearer ") ? token.substring(7).trim() : token.trim();
            String decoded = new String(Base64.getUrlDecoder().decode(cleanToken), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            if (parts.length != 3) {
                return null;
            }
            String username = parts[0];
            long expiry = Long.parseLong(parts[1]);
            String signature = parts[2];
            if (Instant.now().getEpochSecond() > expiry) {
                return null;
            }
            if (!sign(username + ":" + expiry).equals(signature)) {
                return null;
            }
            return username;
        } catch (Exception e) {
            return null;
        }
    }

    public String validatePreAuthToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(token.trim()), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            if (parts.length != 4 || !"PRE_AUTH".equals(parts[0])) {
                return null;
            }
            String username = parts[1];
            long expiry = Long.parseLong(parts[2]);
            String signature = parts[3];
            if (Instant.now().getEpochSecond() > expiry) {
                return null;
            }
            if (!sign("PRE_AUTH:" + username + ":" + expiry).equals(signature)) {
                return null;
            }
            return username;
        } catch (Exception e) {
            return null;
        }
    }

    private String sign(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((payload + ":" + secret).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
