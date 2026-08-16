package com.portfolio.infrastructure.persistence.repository;

import com.portfolio.infrastructure.persistence.entity.KnowledgeArticleEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeArticleJpaRepository extends JpaRepository<KnowledgeArticleEntity, Long> {
    List<KnowledgeArticleEntity> findByStatusOrderByPublishedAtDesc(KnowledgeArticleEntity.Status status);

    Optional<KnowledgeArticleEntity> findBySlugAndStatus(String slug, KnowledgeArticleEntity.Status status);

    long countByStatus(KnowledgeArticleEntity.Status status);
}
