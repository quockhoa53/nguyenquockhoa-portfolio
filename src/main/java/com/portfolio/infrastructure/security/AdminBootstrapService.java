package com.portfolio.infrastructure.security;

import com.portfolio.infrastructure.persistence.entity.AdminAllowedIpEntity;
import com.portfolio.infrastructure.persistence.entity.AdminUserEntity;
import com.portfolio.infrastructure.persistence.repository.AdminAllowedIpJpaRepository;
import com.portfolio.infrastructure.persistence.repository.AdminUserJpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminBootstrapService {
    private final AdminUserJpaRepository admins;
    private final AdminAllowedIpJpaRepository allowedIps;
    private final PasswordEncoder passwordEncoder;

    public AdminBootstrapService(
            AdminUserJpaRepository admins,
            AdminAllowedIpJpaRepository allowedIps,
            PasswordEncoder passwordEncoder) {
        this.admins = admins;
        this.allowedIps = allowedIps;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void bootstrap(String username, String password, String displayName, String ipList) {
        var admin = admins.findByUsername(username)
                .orElseGet(() ->
                        admins.save(new AdminUserEntity(username, passwordEncoder.encode(password), displayName)));
        allowedIps.deleteByAdminIdAndDescription(admin.getId(), "Bootstrap allowlist");
        
        var resolvedIpList = (ipList == null || ipList.isBlank()) ? "*" : ipList;
        for (String ip : resolvedIpList.split(",")) {
            var normalizedIp = ip.trim();
            if (!normalizedIp.isBlank() && !allowedIps.existsByAdminIdAndIpAddress(admin.getId(), normalizedIp)) {
                allowedIps.save(new AdminAllowedIpEntity(admin, normalizedIp, "Bootstrap allowlist"));
            }
        }
    }
}
