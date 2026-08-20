package com.portfolio.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http, AdminTokenFilter adminTokenFilter, RateLimitingFilter rateLimitingFilter)
            throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .authorizeHttpRequests(auth -> auth.requestMatchers(
                                "/api/v1/admin/auth/login",
                                "/api/v1/admin/auth/verify-2fa",
                                "/api/v1/admin/auth/access-check")
                        .permitAll()
                        .requestMatchers("/api/v1/admin/**")
                        .authenticated()
                        .anyRequest()
                        .permitAll())
                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(adminTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout.logoutUrl("/api/v1/admin/auth/logout"))
                .build();
    }

    @Bean
    CommandLineRunner bootstrapAdmin(
            AdminBootstrapService service,
            @Value("${app.admin.bootstrap-username}") String username,
            @Value("${app.admin.bootstrap-password}") String password,
            @Value("${app.admin.bootstrap-display-name}") String displayName,
            @Value("${app.admin.allowed-ips:*}") String allowedIps) {
        return args -> service.bootstrap(username, password, displayName, allowedIps);
    }

    @Bean
    AdminTokenFilter adminTokenFilter(AdminTokenService tokenService) {
        return new AdminTokenFilter(tokenService);
    }

    static class AdminTokenFilter extends OncePerRequestFilter {
        private final AdminTokenService tokenService;

        AdminTokenFilter(AdminTokenService tokenService) {
            this.tokenService = tokenService;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {

            // Check Bearer Token / X-Admin-Token header
            String token = request.getHeader("X-Admin-Token");
            if (token == null || token.isBlank()) {
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7).trim();
                }
            }

            if (token != null && !token.isBlank()) {
                String username = tokenService.validateToken(token);
                if (username != null) {
                    var auth = new UsernamePasswordAuthenticationToken(
                            username, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }

            chain.doFilter(request, response);
        }
    }

    public static String clientIp(HttpServletRequest request) {
        String[] headers = {"CF-Connecting-IP", "X-Forwarded-For", "X-Real-IP"};
        for (String header : headers) {
            String value = request.getHeader(header);
            if (value != null && !value.isBlank() && !"unknown".equalsIgnoreCase(value)) {
                return value.split(",")[0].trim();
            }
        }
        var remoteAddress = request.getRemoteAddr();
        try {
            if (InetAddress.getByName(remoteAddress).isLoopbackAddress()) {
                return localNetworkIp();
            }
        } catch (Exception ignored) {
            // Keep original address when parsing fails
        }
        return remoteAddress;
    }

    public static String localNetworkIp() {
        var candidates = new ArrayList<NetworkAddress>();
        try {
            var interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                var networkInterface = interfaces.nextElement();
                if (!networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.isVirtual()) {
                    continue;
                }
                var interfaceName =
                        (networkInterface.getName() + " " + networkInterface.getDisplayName()).toLowerCase();
                var wifi = interfaceName.contains("wi-fi")
                        || interfaceName.contains("wifi")
                        || interfaceName.contains("wireless")
                        || interfaceName.contains("wlan");
                var addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    var address = addresses.nextElement();
                    if (address.getHostAddress().indexOf(':') < 0 && address.isSiteLocalAddress()) {
                        candidates.add(new NetworkAddress(address.getHostAddress(), wifi));
                    }
                }
            }
        } catch (Exception ignored) {
            // Fall back to loopback when the operating system does not expose network interfaces.
        }
        return candidates.stream()
                .sorted(Comparator.comparing(NetworkAddress::wifi).reversed())
                .map(NetworkAddress::ip)
                .findFirst()
                .orElse("127.0.0.1");
    }

    private record NetworkAddress(String ip, boolean wifi) {}
}
