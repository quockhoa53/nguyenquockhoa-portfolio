package com.portfolio.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "project_comments")
public class ProjectCommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id")
    private ProjectEntity project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guest_id")
    private GuestVisitorEntity guest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ProjectCommentEntity parent;

    @Column(nullable = false, length = 3000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private KnowledgeCommentEntity.Status status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected ProjectCommentEntity() {}

    public ProjectCommentEntity(
            ProjectEntity project, GuestVisitorEntity guest, ProjectCommentEntity parent, String content) {
        this.project = project;
        this.guest = guest;
        this.parent = parent;
        this.content = content;
        this.status = KnowledgeCommentEntity.Status.PENDING;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public void setStatus(KnowledgeCommentEntity.Status status) {
        this.status = status;
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public GuestVisitorEntity getGuest() {
        return guest;
    }

    public ProjectCommentEntity getParent() {
        return parent;
    }

    public String getContent() {
        return content;
    }

    public KnowledgeCommentEntity.Status getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
