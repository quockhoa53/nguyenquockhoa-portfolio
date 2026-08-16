package com.portfolio.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "projects")
public class ProjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 500)
    private String technologies;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "demo_url", columnDefinition = "TEXT")
    private String demoUrl;

    @Column(name = "source_url", columnDefinition = "TEXT")
    private String sourceUrl;

    @Column(nullable = false)
    private boolean featured;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected ProjectEntity() {}

    public ProjectEntity(
            String title,
            String description,
            String technologies,
            String imageUrl,
            String demoUrl,
            String sourceUrl,
            boolean featured,
            int displayOrder) {
        update(title, description, technologies, imageUrl, demoUrl, sourceUrl, featured, displayOrder);
    }

    public void update(
            String title,
            String description,
            String technologies,
            String imageUrl,
            String demoUrl,
            String sourceUrl,
            boolean featured,
            int displayOrder) {
        this.title = title;
        this.description = description;
        this.technologies = technologies;
        this.imageUrl = imageUrl;
        this.demoUrl = demoUrl;
        this.sourceUrl = sourceUrl;
        this.featured = featured;
        this.displayOrder = displayOrder;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getTechnologies() {
        return technologies;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDemoUrl() {
        return demoUrl;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public boolean isFeatured() {
        return featured;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }
}
