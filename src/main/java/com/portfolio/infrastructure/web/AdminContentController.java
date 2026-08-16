package com.portfolio.infrastructure.web;

import com.portfolio.infrastructure.persistence.entity.*;
import com.portfolio.infrastructure.persistence.repository.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminContentController {
    private final ProfileJpaRepository profiles;
    private final SkillJpaRepository skills;
    private final ExperienceJpaRepository experiences;
    private final ProjectJpaRepository projects;
    private final KnowledgeCategoryJpaRepository categories;
    private final KnowledgeArticleJpaRepository articles;
    private final WorkItemJpaRepository workItems;
    private final ContactMessageJpaRepository contacts;
    private final GuestVisitorJpaRepository guests;
    private final KnowledgeLikeJpaRepository knowledgeLikes;
    private final ProjectLikeJpaRepository projectLikes;
    private final KnowledgeCommentJpaRepository knowledgeComments;
    private final ProjectCommentJpaRepository projectComments;

    public AdminContentController(
            ProfileJpaRepository profiles,
            SkillJpaRepository skills,
            ExperienceJpaRepository experiences,
            ProjectJpaRepository projects,
            KnowledgeCategoryJpaRepository categories,
            KnowledgeArticleJpaRepository articles,
            WorkItemJpaRepository workItems,
            ContactMessageJpaRepository contacts,
            GuestVisitorJpaRepository guests,
            KnowledgeLikeJpaRepository knowledgeLikes,
            ProjectLikeJpaRepository projectLikes,
            KnowledgeCommentJpaRepository knowledgeComments,
            ProjectCommentJpaRepository projectComments) {
        this.profiles = profiles;
        this.skills = skills;
        this.experiences = experiences;
        this.projects = projects;
        this.categories = categories;
        this.articles = articles;
        this.workItems = workItems;
        this.contacts = contacts;
        this.guests = guests;
        this.knowledgeLikes = knowledgeLikes;
        this.projectLikes = projectLikes;
        this.knowledgeComments = knowledgeComments;
        this.projectComments = projectComments;
    }

    @GetMapping("/dashboard")
    public Map<String, Long> dashboard() {
        return Map.of(
                "projects", projects.count(),
                "skills", skills.count(),
                "articles", articles.count(),
                "publishedArticles", articles.countByStatus(KnowledgeArticleEntity.Status.PUBLISHED),
                "guests", guests.count(),
                "likes", knowledgeLikes.count() + projectLikes.count(),
                "pendingComments",
                        knowledgeComments.countByStatus(KnowledgeCommentEntity.Status.PENDING)
                                + projectComments.countByStatus(KnowledgeCommentEntity.Status.PENDING),
                "contacts", contacts.count(),
                "workItems", workItems.count());
    }

    @PutMapping("/profile")
    public void updateProfile(@Valid @RequestBody ProfileRequest body) {
        var profile = profiles.findFirstByOrderByIdAsc().orElseThrow();
        profile.update(
                body.fullName(),
                body.headline(),
                Jsoup.clean(body.shortBio(), Safelist.none()),
                cleanRich(body.bio()),
                body.email(),
                body.phone(),
                body.location(),
                body.avatarUrl(),
                body.githubUrl(),
                body.linkedinUrl(),
                body.facebookUrl());
        profiles.save(profile);
    }

    @PostMapping("/skills")
    @ResponseStatus(HttpStatus.CREATED)
    public Long createSkill(@Valid @RequestBody SkillRequest body) {
        return skills.save(new SkillEntity(body.name(), body.category(), body.proficiency(), body.displayOrder()))
                .getId();
    }

    @PutMapping("/skills/{id}")
    public void updateSkill(@PathVariable long id, @Valid @RequestBody SkillRequest body) {
        var entity = skills.findById(id).orElseThrow();
        entity.update(body.name(), body.category(), body.proficiency(), body.displayOrder());
        skills.save(entity);
    }

    @DeleteMapping("/skills/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSkill(@PathVariable long id) {
        skills.deleteById(id);
    }

    @PostMapping("/experiences")
    @ResponseStatus(HttpStatus.CREATED)
    public Long createExperience(@Valid @RequestBody ExperienceRequest body) {
        return experiences
                .save(new ExperienceEntity(
                        body.company(),
                        body.position(),
                        body.startDate(),
                        body.endDate(),
                        cleanRich(body.description()),
                        body.displayOrder()))
                .getId();
    }

    @PutMapping("/experiences/{id}")
    public void updateExperience(@PathVariable long id, @Valid @RequestBody ExperienceRequest body) {
        var entity = experiences.findById(id).orElseThrow();
        entity.update(
                body.company(),
                body.position(),
                body.startDate(),
                body.endDate(),
                cleanRich(body.description()),
                body.displayOrder());
        experiences.save(entity);
    }

    @DeleteMapping("/experiences/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExperience(@PathVariable long id) {
        experiences.deleteById(id);
    }

    @PostMapping("/projects")
    @ResponseStatus(HttpStatus.CREATED)
    public Long createProject(@Valid @RequestBody ProjectRequest body) {
        return projects.save(new ProjectEntity(
                        body.title(),
                        cleanRich(body.description()),
                        body.technologies(),
                        body.imageUrl(),
                        body.demoUrl(),
                        body.sourceUrl(),
                        body.featured(),
                        body.displayOrder()))
                .getId();
    }

    @PutMapping("/projects/{id}")
    public void updateProject(@PathVariable long id, @Valid @RequestBody ProjectRequest body) {
        var entity = projects.findById(id).orElseThrow();
        entity.update(
                body.title(),
                cleanRich(body.description()),
                body.technologies(),
                body.imageUrl(),
                body.demoUrl(),
                body.sourceUrl(),
                body.featured(),
                body.displayOrder());
        projects.save(entity);
    }

    @DeleteMapping("/projects/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@PathVariable long id) {
        projects.deleteById(id);
    }

    @PostMapping("/knowledge/categories")
    public Long createCategory(@Valid @RequestBody CategoryRequest body) {
        return categories
                .save(new KnowledgeCategoryEntity(body.name(), body.slug(), body.description(), body.displayOrder()))
                .getId();
    }

    @PutMapping("/knowledge/categories/{id}")
    public void updateCategory(@PathVariable long id, @Valid @RequestBody CategoryRequest body) {
        var entity = categories.findById(id).orElseThrow();
        entity.update(body.name(), body.slug(), body.description(), body.displayOrder());
        categories.save(entity);
    }

    @DeleteMapping("/knowledge/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable long id) {
        categories.deleteById(id);
    }

    @GetMapping("/knowledge/articles")
    @Transactional(readOnly = true)
    public List<AdminArticleResponse> adminArticles() {
        return articles.findAll().stream()
                .map(article -> new AdminArticleResponse(
                        article.getId(),
                        article.getCategory().getId(),
                        article.getTitle(),
                        article.getSlug(),
                        article.getSummary(),
                        article.getContent(),
                        article.getThumbnailUrl(),
                        article.getStatus().name(),
                        article.isFeatured()))
                .toList();
    }

    @PostMapping("/knowledge/articles")
    public Long createArticle(@Valid @RequestBody ArticleRequest body) {
        var category = categories.findById(body.categoryId()).orElseThrow();
        var entity = new KnowledgeArticleEntity(
                category,
                body.title(),
                body.slug(),
                body.summary(),
                cleanRich(body.content()),
                body.thumbnailUrl(),
                KnowledgeArticleEntity.Status.valueOf(body.status()),
                body.featured());
        return articles.save(entity).getId();
    }

    @PutMapping("/knowledge/articles/{id}")
    public void updateArticle(@PathVariable long id, @Valid @RequestBody ArticleRequest body) {
        var entity = articles.findById(id).orElseThrow();
        entity.update(
                categories.findById(body.categoryId()).orElseThrow(),
                body.title(),
                body.slug(),
                body.summary(),
                cleanRich(body.content()),
                body.thumbnailUrl(),
                KnowledgeArticleEntity.Status.valueOf(body.status()),
                body.featured());
        articles.save(entity);
    }

    @DeleteMapping("/knowledge/articles/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteArticle(@PathVariable long id) {
        articles.deleteById(id);
    }

    @GetMapping("/work-items")
    public List<WorkResponse> workItems() {
        return workItems.findAllByOrderByDisplayOrderAscIdAsc().stream().map(this::toWorkResponse).toList();
    }

    @PostMapping("/work-items")
    public Long createWorkItem(@Valid @RequestBody WorkRequest body) {
        String resolvedSlug = (body.slug() == null || body.slug().isBlank())
                ? slugify(body.title())
                : body.slug().trim();
        boolean isPublished = body.published() != null ? body.published() : true;

        return workItems
                .save(new WorkItemEntity(
                        resolvedSlug,
                        body.period() == null ? "" : body.period(),
                        body.role() == null ? "" : body.role(),
                        body.company() == null ? "" : body.company(),
                        body.title() == null ? "" : body.title(),
                        body.summary() == null ? "" : body.summary(),
                        cleanRich(body.content()),
                        body.technologies() == null ? "" : body.technologies(),
                        body.displayOrder(),
                        isPublished))
                .getId();
    }

    @PutMapping("/work-items/{id}")
    public void updateWorkItem(@PathVariable long id, @Valid @RequestBody WorkRequest body) {
        var entity = workItems.findById(id).orElseThrow();
        String resolvedSlug = (body.slug() == null || body.slug().isBlank())
                ? slugify(body.title())
                : body.slug().trim();
        boolean isPublished = body.published() != null ? body.published() : true;

        entity.update(
                resolvedSlug,
                body.period() == null ? "" : body.period(),
                body.role() == null ? "" : body.role(),
                body.company() == null ? "" : body.company(),
                body.title() == null ? "" : body.title(),
                body.summary() == null ? "" : body.summary(),
                cleanRich(body.content()),
                body.technologies() == null ? "" : body.technologies(),
                body.displayOrder(),
                isPublished);
        workItems.save(entity);
    }

    @DeleteMapping("/work-items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWorkItem(@PathVariable long id) {
        workItems.deleteById(id);
    }

    private String slugify(String text) {
        if (text == null || text.isBlank()) return "work-item-" + System.currentTimeMillis();
        String normalized = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD);
        String slug = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        return slug.isBlank() ? "work-item-" + System.currentTimeMillis() : slug;
    }

    @GetMapping("/comments")
    @Transactional(readOnly = true)
    public List<AdminCommentResponse> comments() {
        var knowledge = knowledgeComments.findAll().stream()
                .map(comment -> new AdminCommentResponse(
                        "KNOWLEDGE",
                        comment.getId(),
                        comment.getGuest().getDisplayName(),
                        comment.getGuest().getEmail(),
                        comment.getContent(),
                        comment.getStatus().name(),
                        comment.getCreatedAt()))
                .toList();
        var project = projectComments.findAll().stream()
                .map(comment -> new AdminCommentResponse(
                        "PROJECT",
                        comment.getId(),
                        comment.getGuest().getDisplayName(),
                        comment.getGuest().getEmail(),
                        comment.getContent(),
                        comment.getStatus().name(),
                        comment.getCreatedAt()))
                .toList();
        return java.util.stream.Stream.concat(knowledge.stream(), project.stream())
                .toList();
    }

    @PatchMapping("/comments/{type}/{id}")
    public void moderate(@PathVariable String type, @PathVariable long id, @RequestBody Map<String, String> body) {
        var status = KnowledgeCommentEntity.Status.valueOf(body.get("status"));
        if ("KNOWLEDGE".equalsIgnoreCase(type)) {
            var comment = knowledgeComments.findById(id).orElseThrow();
            comment.setStatus(status);
            knowledgeComments.save(comment);
        } else {
            var comment = projectComments.findById(id).orElseThrow();
            comment.setStatus(status);
            projectComments.save(comment);
        }
    }

    @GetMapping("/contacts")
    public List<ContactResponse> contacts() {
        return contacts.findAll().stream()
                .map(contact -> new ContactResponse(
                        contact.getId(),
                        contact.getName(),
                        contact.getEmail(),
                        contact.getSubject(),
                        contact.getMessage(),
                        contact.getCreatedAt()))
                .toList();
    }

    @GetMapping("/guests")
    public List<GuestResponse> guests() {
        return guests.findAll().stream()
                .map(guest -> new GuestResponse(
                        guest.getId().toString(), guest.getDisplayName(), guest.getEmail(), guest.isEmailVerified()))
                .toList();
    }

    @GetMapping("/likes")
    @Transactional(readOnly = true)
    public List<LikeAdminResponse> likes() {
        var articleLikes = knowledgeLikes.findAll().stream()
                .map(like -> new LikeAdminResponse(
                        "KNOWLEDGE",
                        like.getId(),
                        like.getArticle().getTitle(),
                        like.getGuest().getDisplayName(),
                        like.getGuest().getEmail(),
                        like.getCreatedAt()))
                .toList();
        var projectLikeList = projectLikes.findAll().stream()
                .map(like -> new LikeAdminResponse(
                        "PROJECT",
                        like.getId(),
                        like.getProject().getTitle(),
                        like.getGuest().getDisplayName(),
                        like.getGuest().getEmail(),
                        like.getCreatedAt()))
                .toList();
        return java.util.stream.Stream.concat(articleLikes.stream(), projectLikeList.stream())
                .toList();
    }

    @DeleteMapping("/contacts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteContact(@PathVariable long id) {
        contacts.deleteById(id);
    }

    private String cleanRich(String html) {
        return Jsoup.clean(html == null ? "" : html, Safelist.relaxed().addAttributes("a", "target", "rel"));
    }

    private WorkResponse toWorkResponse(WorkItemEntity item) {
        return new WorkResponse(
                item.getId(),
                item.getSlug(),
                item.getPeriod(),
                item.getRole(),
                item.getCompany(),
                item.getTitle(),
                item.getSummary(),
                item.getContent(),
                item.getTechnologies(),
                item.getDisplayOrder(),
                item.isPublished());
    }

    public record ProfileRequest(
            @NotBlank String fullName,
            @NotBlank String headline,
            @NotBlank String shortBio,
            @NotBlank String bio,
            @Email String email,
            String phone,
            String location,
            String avatarUrl,
            String githubUrl,
            String linkedinUrl,
            String facebookUrl) {}

    public record SkillRequest(
            @NotBlank String name, @NotBlank String category, @Min(0) @Max(100) int proficiency, int displayOrder) {}

    public record ExperienceRequest(
            @NotBlank String company,
            @NotBlank String position,
            @NotNull LocalDate startDate,
            LocalDate endDate,
            @NotBlank String description,
            int displayOrder) {}

    public record ProjectRequest(
            @NotBlank String title,
            @NotBlank String description,
            @NotBlank String technologies,
            String imageUrl,
            String demoUrl,
            String sourceUrl,
            boolean featured,
            int displayOrder) {}

    public record CategoryRequest(@NotBlank String name, @NotBlank String slug, String description, int displayOrder) {}

    public record ArticleRequest(
            @NotNull Long categoryId,
            @NotBlank String title,
            @NotBlank String slug,
            String summary,
            @NotBlank String content,
            String thumbnailUrl,
            @NotBlank String status,
            boolean featured) {}

    public record WorkRequest(
            String slug,
            @NotBlank String period,
            @NotBlank String role,
            @NotBlank String company,
            @NotBlank String title,
            String summary,
            String content,
            String technologies,
            int displayOrder,
            Boolean published) {}

    public record WorkResponse(
            Long id,
            String slug,
            String period,
            String role,
            String company,
            String title,
            String summary,
            String content,
            String technologies,
            int displayOrder,
            boolean published) {}

    public record AdminArticleResponse(
            Long id,
            Long categoryId,
            String title,
            String slug,
            String summary,
            String content,
            String thumbnailUrl,
            String status,
            boolean featured) {}

    public record AdminCommentResponse(
            String type,
            Long id,
            String displayName,
            String email,
            String content,
            String status,
            java.time.OffsetDateTime createdAt) {}

    public record ContactResponse(
            Long id, String name, String email, String subject, String message, java.time.OffsetDateTime createdAt) {}

    public record GuestResponse(String id, String displayName, String email, boolean emailVerified) {}

    public record LikeAdminResponse(
            String type, Long id, String title, String displayName, String email, java.time.OffsetDateTime createdAt) {}
}
