package com.portfolio.infrastructure.web;

import com.portfolio.infrastructure.persistence.entity.KnowledgeArticleEntity;
import com.portfolio.infrastructure.persistence.entity.KnowledgeCommentEntity;
import com.portfolio.infrastructure.persistence.repository.*;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/knowledge")
public class KnowledgeController {
    private final KnowledgeCategoryJpaRepository categories;
    private final KnowledgeArticleJpaRepository articles;
    private final KnowledgeLikeJpaRepository likes;
    private final KnowledgeCommentJpaRepository comments;

    public KnowledgeController(
            KnowledgeCategoryJpaRepository categories,
            KnowledgeArticleJpaRepository articles,
            KnowledgeLikeJpaRepository likes,
            KnowledgeCommentJpaRepository comments) {
        this.categories = categories;
        this.articles = articles;
        this.likes = likes;
        this.comments = comments;
    }

    @GetMapping("/categories")
    @Cacheable("knowledge_categories")
    public List<CategoryResponse> categories() {
        return categories.findAllByOrderByDisplayOrderAscIdAsc().stream()
                .map(category -> new CategoryResponse(
                        category.getId(),
                        category.getName(),
                        category.getSlug(),
                        category.getDescription(),
                        category.getDisplayOrder()))
                .toList();
    }

    @GetMapping("/articles")
    @Cacheable("knowledge_articles")
    @Transactional(readOnly = true)
    public List<ArticleSummary> articles() {
        return articles.findByStatusOrderByPublishedAtDesc(KnowledgeArticleEntity.Status.PUBLISHED).stream()
                .map(this::summary)
                .toList();
    }

    @GetMapping("/articles/{slug}")
    @Cacheable(value = "knowledge_article_detail", key = "#slug")
    @Transactional
    public ArticleDetail article(@PathVariable String slug) {
        var article = articles.findBySlugAndStatus(slug, KnowledgeArticleEntity.Status.PUBLISHED)
                .orElseThrow(ResourceNotFoundException::new);
        articles.incrementViewCountById(article.getId());
        return new ArticleDetail(summary(article), article.getContent());
    }

    private ArticleSummary summary(KnowledgeArticleEntity article) {
        return new ArticleSummary(
                article.getId(),
                article.getTitle(),
                article.getSlug(),
                article.getSummary(),
                article.getThumbnailUrl(),
                article.getCategory().getName(),
                article.getCategory().getSlug(),
                article.isFeatured(),
                article.getViewCount() + 1,
                likes.countByArticleId(article.getId()),
                comments.countByArticleIdAndStatusIn(
                        article.getId(),
                        java.util.List.of(
                                KnowledgeCommentEntity.Status.APPROVED, KnowledgeCommentEntity.Status.PENDING)),
                article.getPublishedAt());
    }

    public record CategoryResponse(Long id, String name, String slug, String description, int displayOrder) {}

    public record ArticleSummary(
            Long id,
            String title,
            String slug,
            String summary,
            String thumbnailUrl,
            String categoryName,
            String categorySlug,
            boolean featured,
            long viewCount,
            long likeCount,
            long commentCount,
            OffsetDateTime publishedAt) {}

    public record ArticleDetail(ArticleSummary article, String content) {}

    @ResponseStatus(org.springframework.http.HttpStatus.NOT_FOUND)
    static class ResourceNotFoundException extends RuntimeException {}
}
