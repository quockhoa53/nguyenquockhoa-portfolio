package com.portfolio.application.service;

import com.portfolio.infrastructure.persistence.entity.GuestVisitorEntity;
import com.portfolio.infrastructure.persistence.repository.GuestVisitorJpaRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuestIdentityService {
    private static final String COOKIE_NAME = "portfolio_guest";
    private final GuestVisitorJpaRepository guests;
    private final byte[] secret;

    public GuestIdentityService(GuestVisitorJpaRepository guests, @Value("${app.guest.cookie-secret}") String secret) {
        this.guests = guests;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    @Transactional
    public GuestVisitorEntity register(String displayName, String email, HttpServletResponse response) {
        var normalizedEmail = email.trim().toLowerCase();
        var guest = guests.save(new GuestVisitorEntity(
                UUID.randomUUID(), displayName.trim(), normalizedEmail, sha256(normalizedEmail)));
        var cookie = new Cookie(COOKIE_NAME, sign(guest.getId()));
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(365 * 24 * 60 * 60);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
        return guest;
    }

    public GuestVisitorEntity requireGuest(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    return verify(cookie.getValue());
                }
            }
        }
        throw new GuestRequiredException();
    }

    private GuestVisitorEntity verify(String token) {
        var parts = token.split("\\.");
        if (parts.length != 2
                || !MessageDigest.isEqual(parts[1].getBytes(), hmac(parts[0]).getBytes())) {
            throw new GuestRequiredException();
        }
        try {
            return guests.findById(UUID.fromString(parts[0])).orElseThrow(GuestRequiredException::new);
        } catch (IllegalArgumentException exception) {
            throw new GuestRequiredException();
        }
    }

    private String sign(UUID id) {
        return id + "." + hmac(id.toString());
    }

    private String hmac(String value) {
        try {
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot sign guest identity", exception);
        }
    }

    private String sha256(String value) {
        try {
            return HexFormat.of()
                    .formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.UNAUTHORIZED)
    public static class GuestRequiredException extends RuntimeException {}
}
