package com.portfolio.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "work_items")
public class WorkItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 180)
    private String slug;

    @Column(nullable = false, length = 100)
    private String period;

    @Column(nullable = false, length = 150)
    private String role;

    @Column(nullable = false, length = 200)
    private String company;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String summary;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 1000)
    private String technologies;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private boolean published;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected WorkItemEntity() {}

    public WorkItemEntity(
            String slug,
            String period,
            String role,
            String company,
            String title,
            String summary,
            String content,
            String technologies,
            int displayOrder,
            boolean published) {
        this.createdAt = OffsetDateTime.now();
        update(slug, period, role, company, title, summary, content, technologies, displayOrder, published);
    }

    public void update(
            String slug,
            String period,
            String role,
            String company,
            String title,
            String summary,
            String content,
            String technologies,
            int displayOrder,
            boolean published) {
        this.slug = slug;
        this.period = period;
        this.role = role;
        this.company = company;
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.technologies = technologies;
        this.displayOrder = displayOrder;
        this.published = published;
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getPeriod() {
        return period;
    }

    public String getRole() {
        return role;
    }

    public String getCompany() {
        return company;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getContent() {
        return content;
    }

    public String getTechnologies() {
        return technologies;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public boolean isPublished() {
        return published;
    }
}
