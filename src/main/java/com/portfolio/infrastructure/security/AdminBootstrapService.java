package com.portfolio.infrastructure.security;

import com.portfolio.infrastructure.persistence.entity.AdminAllowedIpEntity;
import com.portfolio.infrastructure.persistence.entity.AdminUserEntity;
import com.portfolio.infrastructure.persistence.entity.SkillEntity;
import com.portfolio.infrastructure.persistence.repository.AdminAllowedIpJpaRepository;
import com.portfolio.infrastructure.persistence.repository.AdminUserJpaRepository;
import com.portfolio.infrastructure.persistence.repository.SkillJpaRepository;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminBootstrapService {
    private final AdminUserJpaRepository admins;
    private final AdminAllowedIpJpaRepository allowedIps;
    private final SkillJpaRepository skills;
    private final PasswordEncoder passwordEncoder;

    public AdminBootstrapService(
            AdminUserJpaRepository admins,
            AdminAllowedIpJpaRepository allowedIps,
            SkillJpaRepository skills,
            PasswordEncoder passwordEncoder) {
        this.admins = admins;
        this.allowedIps = allowedIps;
        this.skills = skills;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void bootstrap(String username, String password, String displayName, String ipList) {
        var admin = admins.findByUsername(username)
                .orElseGet(() ->
                        admins.save(new AdminUserEntity(username, passwordEncoder.encode(password), displayName)));
        allowedIps.deleteByAdminIdAndDescription(admin.getId(), "Bootstrap allowlist");
        var resolvedIpList = ipList.isBlank() ? SecurityConfig.localNetworkIp() : ipList;
        for (String ip : resolvedIpList.split(",")) {
            var normalizedIp = ip.trim();
            if (!normalizedIp.isBlank() && !allowedIps.existsByAdminIdAndIpAddress(admin.getId(), normalizedIp)) {
                allowedIps.save(new AdminAllowedIpEntity(admin, normalizedIp, "Bootstrap allowlist"));
            }
        }

        bootstrapSkillCategories();
    }

    private void bootstrapSkillCategories() {
        var allSkills = skills.findAll();
        for (SkillEntity s : allSkills) {
            if ("Backend".equalsIgnoreCase(s.getCategory())) {
                s.update(s.getName(), "Backend & Architecture", s.getProficiency(), s.getDisplayOrder());
                skills.save(s);
            } else if ("Frontend".equalsIgnoreCase(s.getCategory())) {
                s.update(s.getName(), "AI & Tools", s.getProficiency(), s.getDisplayOrder());
                skills.save(s);
            }
        }

        // Check if any of the 4 standard categories is empty, and seed defaults
        ensureCategoryHasSkills("Backend & Architecture", List.of(
                new SkillEntity("Java Spring Boot", "Backend & Architecture", 90, 1),
                new SkillEntity("RESTful API & Microservices", "Backend & Architecture", 85, 2)
        ));

        ensureCategoryHasSkills("Database", List.of(
                new SkillEntity("PostgreSQL & MySQL", "Database", 85, 1),
                new SkillEntity("Query Optimization", "Database", 80, 2)
        ));

        ensureCategoryHasSkills("Data Processing", List.of(
                new SkillEntity("Apache Flink & Streaming", "Data Processing", 80, 1),
                new SkillEntity("CDC & Data Mapping", "Data Processing", 80, 2)
        ));

        ensureCategoryHasSkills("AI & Tools", List.of(
                new SkillEntity("AI Agent & LLM Integration", "AI & Tools", 85, 1),
                new SkillEntity("Docker & CI/CD Pipelines", "AI & Tools", 80, 2)
        ));
    }

    private void ensureCategoryHasSkills(String categoryName, List<SkillEntity> defaultSkills) {
        boolean exists = skills.findAll().stream()
                .anyMatch(s -> categoryName.equalsIgnoreCase(s.getCategory()));
        if (!exists) {
            skills.saveAll(defaultSkills);
        }
    }
}
