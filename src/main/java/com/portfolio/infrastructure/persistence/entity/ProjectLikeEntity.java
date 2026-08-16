package com.portfolio.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "project_likes")
public class ProjectLikeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id")
    private ProjectEntity project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guest_id")
    private GuestVisitorEntity guest;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected ProjectLikeEntity() {}

    public ProjectLikeEntity(ProjectEntity project, GuestVisitorEntity guest) {
        this.project = project;
        this.guest = guest;
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public GuestVisitorEntity getGuest() {
        return guest;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
