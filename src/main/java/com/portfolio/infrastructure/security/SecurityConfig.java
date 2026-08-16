package com.portfolio.infrastructure.security;

import com.portfolio.infrastructure.persistence.repository.AdminAllowedIpJpaRepository;
import com.portfolio.infrastructure.persistence.repository.AdminUserJpaRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Comparator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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
    SecurityFilterChain securityFilterChain(HttpSecurity http, AdminIpFilter adminIpFilter) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .authorizeHttpRequests(
                        auth -> auth.requestMatchers("/api/v1/admin/auth/login", "/api/v1/admin/auth/access-check")
                                .permitAll()
                                .requestMatchers("/api/v1/admin/**")
                                .authenticated()
                                .anyRequest()
                                .permitAll())
                .addFilterAfter(adminIpFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout.logoutUrl("/api/v1/admin/auth/logout"))
                .build();
    }

    @Bean
    CommandLineRunner bootstrapAdmin(
            AdminBootstrapService service,
            @Value("${app.admin.bootstrap-username}") String username,
            @Value("${app.admin.bootstrap-password}") String password,
            @Value("${app.admin.bootstrap-display-name}") String displayName,
            @Value("${app.admin.allowed-ips}") String allowedIps) {
        return args -> service.bootstrap(username, password, displayName, allowedIps);
    }

    @Bean
    AdminIpFilter adminIpFilter(AdminUserJpaRepository admins, AdminAllowedIpJpaRepository allowedIps) {
        return new AdminIpFilter(admins, allowedIps);
    }

    static class AdminIpFilter extends OncePerRequestFilter {
        private final AdminUserJpaRepository admins;
        private final AdminAllowedIpJpaRepository allowedIps;

        AdminIpFilter(AdminUserJpaRepository admins, AdminAllowedIpJpaRepository allowedIps) {
            this.admins = admins;
            this.allowedIps = allowedIps;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext()
                    .getAuthentication();
            if (request.getRequestURI().startsWith("/api/v1/admin/")
                    && !request.getRequestURI().equals("/api/v1/admin/auth/access-check")
                    && authentication != null
                    && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getName())) {
                var admin = admins.findByUsername(authentication.getName()).orElse(null);
                if (admin == null || !allowedIps.existsByAdminIdAndIpAddress(admin.getId(), clientIp(request))) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "IP không được phép truy cập tài khoản admin");
                    return;
                }
            }
            chain.doFilter(request, response);
        }
    }

    public static String clientIp(HttpServletRequest request) {
        var remoteAddress = request.getRemoteAddr();
        try {
            if (InetAddress.getByName(remoteAddress).isLoopbackAddress()) {
                return localNetworkIp();
            }
        } catch (Exception ignored) {
            // Keep the original address when the servlet container returns a non-IP value.
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
