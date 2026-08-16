package com.portfolio.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "knowledge_article_likes")
public class KnowledgeLikeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id")
    private KnowledgeArticleEntity article;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guest_id")
    private GuestVisitorEntity guest;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected KnowledgeLikeEntity() {}

    public KnowledgeLikeEntity(KnowledgeArticleEntity article, GuestVisitorEntity guest) {
        this.article = article;
        this.guest = guest;
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public KnowledgeArticleEntity getArticle() {
        return article;
    }

    public GuestVisitorEntity getGuest() {
        return guest;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
