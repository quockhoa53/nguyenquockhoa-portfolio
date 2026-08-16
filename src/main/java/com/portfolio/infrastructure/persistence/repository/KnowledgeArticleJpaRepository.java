package com.portfolio.infrastructure.persistence.repository;

import com.portfolio.infrastructure.persistence.entity.KnowledgeArticleEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KnowledgeArticleJpaRepository extends JpaRepository<KnowledgeArticleEntity, Long> {

    @Query("SELECT a FROM KnowledgeArticleEntity a JOIN FETCH a.category WHERE a.status = :status ORDER BY a.publishedAt DESC")
    List<KnowledgeArticleEntity> findByStatusOrderByPublishedAtDesc(@Param("status") KnowledgeArticleEntity.Status status);

    @Query("SELECT a FROM KnowledgeArticleEntity a JOIN FETCH a.category WHERE a.slug = :slug AND a.status = :status")
    Optional<KnowledgeArticleEntity> findBySlugAndStatus(@Param("slug") String slug, @Param("status") KnowledgeArticleEntity.Status status);

    @Modifying
    @Query("UPDATE KnowledgeArticleEntity a SET a.viewCount = a.viewCount + 1 WHERE a.id = :id")
    void incrementViewCountById(@Param("id") Long id);

    long countByStatus(KnowledgeArticleEntity.Status status);
}
