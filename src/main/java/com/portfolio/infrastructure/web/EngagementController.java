package com.portfolio.infrastructure.web;

import com.portfolio.application.service.GuestIdentityService;
import com.portfolio.infrastructure.persistence.entity.*;
import com.portfolio.infrastructure.persistence.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;
import org.jsoup.Jsoup;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class EngagementController {
    private final GuestIdentityService identities;
    private final KnowledgeArticleJpaRepository articles;
    private final ProjectJpaRepository projects;
    private final KnowledgeLikeJpaRepository knowledgeLikes;
    private final ProjectLikeJpaRepository projectLikes;
    private final KnowledgeCommentJpaRepository knowledgeComments;
    private final ProjectCommentJpaRepository projectComments;
    private final CacheManager cacheManager;

    public EngagementController(
            GuestIdentityService identities,
            KnowledgeArticleJpaRepository articles,
            ProjectJpaRepository projects,
            KnowledgeLikeJpaRepository knowledgeLikes,
            ProjectLikeJpaRepository projectLikes,
            KnowledgeCommentJpaRepository knowledgeComments,
            ProjectCommentJpaRepository projectComments,
            CacheManager cacheManager) {
        this.identities = identities;
        this.articles = articles;
        this.projects = projects;
        this.knowledgeLikes = knowledgeLikes;
        this.projectLikes = projectLikes;
        this.knowledgeComments = knowledgeComments;
        this.projectComments = projectComments;
        this.cacheManager = cacheManager;
    }

    private void evictCache(String... names) {
        if (cacheManager != null) {
            for (String name : names) {
                var c = cacheManager.getCache(name);
                if (c != null) {
                    c.clear();
                }
            }
        }
    }

    @GetMapping("/knowledge/articles/{id}/like")
    @Transactional(readOnly = true)
    public LikeResponse getArticleLikeStatus(@PathVariable long id, HttpServletRequest request) {
        boolean liked = false;
        try {
            var guest = identities.requireGuest(request);
            liked = knowledgeLikes.findByArticleIdAndGuestId(id, guest.getId()).isPresent();
        } catch (Exception ignored) {
        }
        return new LikeResponse(liked, knowledgeLikes.countByArticleId(id));
    }

    @PutMapping("/knowledge/articles/{id}/like")
    @Transactional
    public LikeResponse likeArticle(@PathVariable long id, HttpServletRequest request) {
        var guest = identities.requireGuest(request);
        knowledgeLikes
                .findByArticleIdAndGuestId(id, guest.getId())
                .orElseGet(() -> knowledgeLikes.save(new KnowledgeLikeEntity(articles.getReferenceById(id), guest)));
        evictCache("knowledge_articles", "knowledge_article_detail");
        return new LikeResponse(true, knowledgeLikes.countByArticleId(id));
    }

    @DeleteMapping("/knowledge/articles/{id}/like")
    @Transactional
    public LikeResponse unlikeArticle(@PathVariable long id, HttpServletRequest request) {
        var guest = identities.requireGuest(request);
        knowledgeLikes.findByArticleIdAndGuestId(id, guest.getId()).ifPresent(knowledgeLikes::delete);
        evictCache("knowledge_articles", "knowledge_article_detail");
        return new LikeResponse(false, knowledgeLikes.countByArticleId(id));
    }

    @GetMapping("/projects/{id}/like")
    @Transactional(readOnly = true)
    public LikeResponse getProjectLikeStatus(@PathVariable long id, HttpServletRequest request) {
        boolean liked = false;
        try {
            var guest = identities.requireGuest(request);
            liked = projectLikes.findByProjectIdAndGuestId(id, guest.getId()).isPresent();
        } catch (Exception ignored) {
        }
        return new LikeResponse(liked, projectLikes.countByProjectId(id));
    }

    @PutMapping("/projects/{id}/like")
    @Transactional
    public LikeResponse likeProject(@PathVariable long id, HttpServletRequest request) {
        var guest = identities.requireGuest(request);
        projectLikes
                .findByProjectIdAndGuestId(id, guest.getId())
                .orElseGet(() -> projectLikes.save(new ProjectLikeEntity(projects.getReferenceById(id), guest)));
        evictCache("portfolio_projects", "portfolio_project_detail");
        return new LikeResponse(true, projectLikes.countByProjectId(id));
    }

    @DeleteMapping("/projects/{id}/like")
    @Transactional
    public LikeResponse unlikeProject(@PathVariable long id, HttpServletRequest request) {
        var guest = identities.requireGuest(request);
        projectLikes.findByProjectIdAndGuestId(id, guest.getId()).ifPresent(projectLikes::delete);
        evictCache("portfolio_projects", "portfolio_project_detail");
        return new LikeResponse(false, projectLikes.countByProjectId(id));
    }

    private static final List<KnowledgeCommentEntity.Status> VISIBLE_STATUSES =
            List.of(KnowledgeCommentEntity.Status.APPROVED, KnowledgeCommentEntity.Status.PENDING);

    @GetMapping("/knowledge/articles/{id}/comments")
    @Transactional(readOnly = true)
    public List<CommentResponse> knowledgeComments(@PathVariable long id) {
        return knowledgeComments.findByArticleIdAndStatusInOrderByCreatedAtAsc(id, VISIBLE_STATUSES).stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping("/knowledge/articles/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public CommentResponse commentArticle(
            @PathVariable long id, @Valid @RequestBody CommentRequest body, HttpServletRequest request) {
        var guest = identities.requireGuest(request);
        var parent = body.parentId() == null
                ? null
                : knowledgeComments.findById(body.parentId()).orElse(null);
        var response = toResponse(knowledgeComments.save(
                new KnowledgeCommentEntity(articles.getReferenceById(id), guest, parent, clean(body.content()))));
        evictCache("knowledge_articles", "knowledge_article_detail");
        return response;
    }

    @GetMapping("/projects/{id}/comments")
    @Transactional(readOnly = true)
    public List<CommentResponse> projectComments(@PathVariable long id) {
        return projectComments.findByProjectIdAndStatusInOrderByCreatedAtAsc(id, VISIBLE_STATUSES).stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping("/projects/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public CommentResponse commentProject(
            @PathVariable long id, @Valid @RequestBody CommentRequest body, HttpServletRequest request) {
        var guest = identities.requireGuest(request);
        var parent = body.parentId() == null
                ? null
                : projectComments.findById(body.parentId()).orElse(null);
        var response = toResponse(projectComments.save(
                new ProjectCommentEntity(projects.getReferenceById(id), guest, parent, clean(body.content()))));
        evictCache("portfolio_projects", "portfolio_project_detail");
        return response;
    }

    private String clean(String value) {
        return Jsoup.clean(value, org.jsoup.safety.Safelist.none());
    }

    private CommentResponse toResponse(KnowledgeCommentEntity c) {
        return new CommentResponse(
                c.getId(),
                c.getGuest().getDisplayName(),
                c.getGuest().isEmailVerified(),
                c.getParent() == null ? null : c.getParent().getId(),
                c.getContent(),
                c.getStatus().name(),
                c.getCreatedAt());
    }

    private CommentResponse toResponse(ProjectCommentEntity c) {
        return new CommentResponse(
                c.getId(),
                c.getGuest().getDisplayName(),
                c.getGuest().isEmailVerified(),
                c.getParent() == null ? null : c.getParent().getId(),
                c.getContent(),
                c.getStatus().name(),
                c.getCreatedAt());
    }

    public record LikeResponse(boolean liked, long likeCount) {}

    public record CommentRequest(@NotBlank @Size(max = 3000) String content, Long parentId) {}

    public record CommentResponse(
            Long id,
            String displayName,
            boolean emailVerified,
            Long parentId,
            String content,
            String status,
            OffsetDateTime createdAt) {}
}
