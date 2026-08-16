package com.portfolio.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "knowledge_article_comments")
public class KnowledgeCommentEntity {
    public enum Status {
        PENDING,
        APPROVED,
        REJECTED,
        SPAM,
        DELETED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id")
    private KnowledgeArticleEntity article;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guest_id")
    private GuestVisitorEntity guest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private KnowledgeCommentEntity parent;

    @Column(nullable = false, length = 3000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Status status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected KnowledgeCommentEntity() {}

    public KnowledgeCommentEntity(
            KnowledgeArticleEntity article, GuestVisitorEntity guest, KnowledgeCommentEntity parent, String content) {
        this.article = article;
        this.guest = guest;
        this.parent = parent;
        this.content = content;
        this.status = Status.PENDING;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public void setStatus(Status status) {
        this.status = status;
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public GuestVisitorEntity getGuest() {
        return guest;
    }

    public KnowledgeCommentEntity getParent() {
        return parent;
    }

    public String getContent() {
        return content;
    }

    public Status getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
