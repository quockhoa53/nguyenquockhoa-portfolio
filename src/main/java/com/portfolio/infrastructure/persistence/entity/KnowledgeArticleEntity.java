package com.portfolio.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "knowledge_articles")
public class KnowledgeArticleEntity {
    public enum Status {
        DRAFT,
        PUBLISHED,
        ARCHIVED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private KnowledgeCategoryEntity category;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true, length = 280)
    private String slug;

    @Column(length = 1000)
    private String summary;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Status status;

    @Column(nullable = false)
    private boolean featured;

    @Column(name = "view_count", nullable = false)
    private long viewCount;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected KnowledgeArticleEntity() {}

    public KnowledgeArticleEntity(
            KnowledgeCategoryEntity category,
            String title,
            String slug,
            String summary,
            String content,
            String thumbnailUrl,
            Status status,
            boolean featured) {
        this.createdAt = OffsetDateTime.now();
        update(category, title, slug, summary, content, thumbnailUrl, status, featured);
    }

    public void update(
            KnowledgeCategoryEntity category,
            String title,
            String slug,
            String summary,
            String content,
            String thumbnailUrl,
            Status status,
            boolean featured) {
        this.category = category;
        this.title = title;
        this.slug = slug;
        this.summary = summary;
        this.content = content;
        this.thumbnailUrl = thumbnailUrl;
        this.status = status;
        this.featured = featured;
        this.updatedAt = OffsetDateTime.now();
        if (status == Status.PUBLISHED && publishedAt == null) {
            publishedAt = OffsetDateTime.now();
        }
    }

    public void incrementViewCount() {
        viewCount++;
    }

    public Long getId() {
        return id;
    }

    public KnowledgeCategoryEntity getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }

    public String getSlug() {
        return slug;
    }

    public String getSummary() {
        return summary;
    }

    public String getContent() {
        return content;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public Status getStatus() {
        return status;
    }

    public boolean isFeatured() {
        return featured;
    }

    public long getViewCount() {
        return viewCount;
    }

    public OffsetDateTime getPublishedAt() {
        return publishedAt;
    }
}
