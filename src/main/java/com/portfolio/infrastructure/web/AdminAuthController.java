package com.portfolio.infrastructure.web;

import com.portfolio.infrastructure.persistence.repository.AdminUserJpaRepository;
import com.portfolio.infrastructure.security.AdminTokenService;
import com.portfolio.infrastructure.security.SecurityConfig;
import com.portfolio.infrastructure.security.TotpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/auth")
public class AdminAuthController {
    private final AdminUserJpaRepository admins;
    private final PasswordEncoder passwordEncoder;
    private final AdminTokenService tokenService;
    private final TotpService totpService;

    public AdminAuthController(
            AdminUserJpaRepository admins,
            PasswordEncoder passwordEncoder,
            AdminTokenService tokenService,
            TotpService totpService) {
        this.admins = admins;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.totpService = totpService;
    }

    @GetMapping("/access-check")
    public AccessCheckResponse accessCheck(HttpServletRequest request) {
        var ip = SecurityConfig.clientIp(request);
        return new AccessCheckResponse(ip, true);
    }

    /**
     * Step 1: Validate Username & Password, return 2FA requirement and temporary preAuthToken.
     */
    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest body, HttpServletRequest request) {
        var admin = admins.findByUsername(body.username())
                .filter(user -> user.isEnabled() && passwordEncoder.matches(body.password(), user.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);

        String preAuthToken = tokenService.generatePreAuthToken(admin.getUsername());
        Map<String, Object> response = new HashMap<>();
        response.put("requiresTotp", true);
        response.put("preAuthToken", preAuthToken);
        response.put("username", admin.getUsername());

        // Check if user has already configured and enabled 2FA
        if (!admin.isTotpEnabled() || admin.getTotpSecret() == null || admin.getTotpSecret().isBlank()) {
            // First-time 2FA Setup
            String secret = (admin.getTotpSecret() != null && !admin.getTotpSecret().isBlank())
                    ? admin.getTotpSecret()
                    : totpService.generateSecret();
            admin.assignPendingTotpSecret(secret);
            admins.save(admin);

            String otpAuthUri = totpService.buildOtpAuthUri(admin.getUsername(), secret);
            response.put("isSetup", true);
            response.put("totpSecret", secret);
            response.put("otpAuthUri", otpAuthUri);
        } else {
            response.put("isSetup", false);
        }

        return response;
    }

    /**
     * Step 2: Validate 6-digit TOTP code and issue final JWT Admin Token.
     */
    @PostMapping("/verify-2fa")
    public Map<String, Object> verify2Fa(@Valid @RequestBody Verify2FaRequest body, HttpServletRequest request) {
        String username = tokenService.validatePreAuthToken(body.preAuthToken());
        if (username == null) {
            throw new PreAuthExpiredException();
        }

        var admin = admins.findByUsername(username)
                .filter(user -> user.isEnabled())
                .orElseThrow(InvalidCredentialsException::new);

        String secret = admin.getTotpSecret();
        if (secret == null || secret.isBlank()) {
            throw new InvalidTotpException("Chưa thiết lập mã 2FA. Vui lòng đăng nhập lại từ đầu.");
        }

        boolean isValid = totpService.verifyCode(secret, body.code());
        if (!isValid) {
            throw new InvalidTotpException("Mã 2FA không chính xác hoặc đã hết hạn. Vui lòng nhập mã mới từ ứng dụng Authenticator.");
        }

        // Activate 2FA if first time
        if (!admin.isTotpEnabled()) {
            admin.setupTotp(secret);
        }
        admin.loggedIn();
        admins.save(admin);

        var authentication = new UsernamePasswordAuthenticationToken(
                admin.getUsername(), null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        request.getSession(true).setAttribute("SPRING_SECURITY_CONTEXT", context);

        String token = tokenService.generateToken(admin.getUsername());

        return Map.of(
                "token", token,
                "username", admin.getUsername(),
                "displayName", admin.getDisplayName(),
                "roles", List.of("ADMIN"),
                "totpEnabled", true
        );
    }

    /**
     * Resets 2FA secret to allow scanning on a new phone (Requires authenticated Admin).
     */
    @PostMapping("/reset-2fa")
    public Map<String, Object> reset2Fa(org.springframework.security.core.Authentication authentication) {
        var admin = admins.findByUsername(authentication.getName()).orElseThrow();
        String newSecret = totpService.generateSecret();
        admin.assignPendingTotpSecret(newSecret);
        admin.resetTotp();
        admins.save(admin);

        String otpAuthUri = totpService.buildOtpAuthUri(admin.getUsername(), newSecret);
        return Map.of(
                "success", true,
                "totpSecret", newSecret,
                "otpAuthUri", otpAuthUri,
                "message", "Đã tạo mã QR 2FA mới. Vui lòng quét vào Google Authenticator để kích hoạt lại."
        );
    }

    @GetMapping("/me")
    public Map<String, Object> me(org.springframework.security.core.Authentication authentication) {
        var admin = admins.findByUsername(authentication.getName()).orElseThrow();
        return Map.of(
                "username", admin.getUsername(),
                "displayName", admin.getDisplayName(),
                "totpEnabled", admin.isTotpEnabled(),
                "totpSetupAt", admin.getTotpSetupAt() != null ? admin.getTotpSetupAt().toString() : ""
        );
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

    public record Verify2FaRequest(@NotBlank String preAuthToken, @NotBlank String code) {}

    public record AccessCheckResponse(String ip, boolean allowed) {}

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    static class InvalidCredentialsException extends RuntimeException {
        InvalidCredentialsException() {
            super("Tài khoản hoặc mật khẩu không chính xác.");
        }
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    static class PreAuthExpiredException extends RuntimeException {
        PreAuthExpiredException() {
            super("Phiên đăng nhập bước 1 đã hết hạn. Vui lòng đăng nhập lại.");
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    static class InvalidTotpException extends RuntimeException {
        InvalidTotpException(String message) {
            super(message);
        }
    }
}
