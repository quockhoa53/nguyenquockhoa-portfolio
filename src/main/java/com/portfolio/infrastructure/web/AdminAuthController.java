package com.portfolio.infrastructure.web;

import com.portfolio.infrastructure.persistence.repository.AdminAllowedIpJpaRepository;
import com.portfolio.infrastructure.persistence.repository.AdminUserJpaRepository;
import com.portfolio.infrastructure.security.SecurityConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
    private final AdminAllowedIpJpaRepository allowedIps;
    private final PasswordEncoder passwordEncoder;

    public AdminAuthController(
            AdminUserJpaRepository admins, AdminAllowedIpJpaRepository allowedIps, PasswordEncoder passwordEncoder) {
        this.admins = admins;
        this.allowedIps = allowedIps;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/access-check")
    public AccessCheckResponse accessCheck(HttpServletRequest request) {
        var ip = SecurityConfig.clientIp(request);
        return new AccessCheckResponse(ip, allowedIps.existsByIpAddress(ip));
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest body, HttpServletRequest request) {
        var admin = admins.findByUsername(body.username())
                .filter(user -> user.isEnabled() && passwordEncoder.matches(body.password(), user.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);
        var ip = SecurityConfig.clientIp(request);
        if (!allowedIps.existsByAdminIdAndIpAddress(admin.getId(), ip)) {
            throw new IpForbiddenException(ip);
        }
        var authentication = new UsernamePasswordAuthenticationToken(
                admin.getUsername(), null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        request.getSession(true).setAttribute("SPRING_SECURITY_CONTEXT", context);
        admin.loggedIn();
        admins.save(admin);
        return Map.of("username", admin.getUsername(), "displayName", admin.getDisplayName(), "ip", ip);
    }

    @GetMapping("/me")
    public Map<String, String> me(org.springframework.security.core.Authentication authentication) {
        var admin = admins.findByUsername(authentication.getName()).orElseThrow();
        return Map.of("username", admin.getUsername(), "displayName", admin.getDisplayName());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        request.getSession(false).invalidate();
        SecurityContextHolder.clearContext();
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

    public record AccessCheckResponse(String ip, boolean allowed) {}

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    static class InvalidCredentialsException extends RuntimeException {}

    @ResponseStatus(HttpStatus.FORBIDDEN)
    static class IpForbiddenException extends RuntimeException {
        IpForbiddenException(String ip) {
            super("IP không được phép: " + ip);
        }
    }
}
