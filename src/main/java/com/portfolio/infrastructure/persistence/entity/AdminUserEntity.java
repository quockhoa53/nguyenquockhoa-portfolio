package com.portfolio.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "admin_users")
public class AdminUserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "totp_secret", length = 100)
    private String totpSecret;

    @Column(name = "totp_enabled", nullable = false)
    private boolean totpEnabled;

    @Column(name = "totp_setup_at")
    private OffsetDateTime totpSetupAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    protected AdminUserEntity() {}

    public AdminUserEntity(String username, String passwordHash, String displayName) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.enabled = true;
        this.totpEnabled = false;
        this.createdAt = OffsetDateTime.now();
    }

    public void update(String displayName, boolean enabled) {
        this.displayName = displayName;
        this.enabled = enabled;
    }

    public void updatePassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setupTotp(String secret) {
        this.totpSecret = secret;
        this.totpEnabled = true;
        this.totpSetupAt = OffsetDateTime.now();
    }

    public void assignPendingTotpSecret(String secret) {
        this.totpSecret = secret;
    }

    public void resetTotp() {
        this.totpSecret = null;
        this.totpEnabled = false;
        this.totpSetupAt = null;
    }

    public void loggedIn() {
        lastLoginAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getTotpSecret() {
        return totpSecret;
    }

    public boolean isTotpEnabled() {
        return totpEnabled;
    }

    public OffsetDateTime getTotpSetupAt() {
        return totpSetupAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getLastLoginAt() {
        return lastLoginAt;
    }
}
