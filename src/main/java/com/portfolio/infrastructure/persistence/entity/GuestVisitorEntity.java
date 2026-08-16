package com.portfolio.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "guest_visitors")
public class GuestVisitorEntity {
    @Id
    private UUID id;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @Column(nullable = false)
    private String email;

    @Column(name = "email_hash", nullable = false, length = 64)
    private String emailHash;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "last_seen_at", nullable = false)
    private OffsetDateTime lastSeenAt;

    protected GuestVisitorEntity() {}

    public GuestVisitorEntity(UUID id, String displayName, String email, String emailHash) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
        this.emailHash = emailHash;
        this.createdAt = OffsetDateTime.now();
        this.lastSeenAt = this.createdAt;
    }

    public void touch() {
        lastSeenAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }
}
