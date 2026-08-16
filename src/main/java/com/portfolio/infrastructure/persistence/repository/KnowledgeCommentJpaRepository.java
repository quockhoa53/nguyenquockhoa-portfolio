package com.portfolio.infrastructure.persistence.repository;

import com.portfolio.infrastructure.persistence.entity.KnowledgeCommentEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeCommentJpaRepository extends JpaRepository<KnowledgeCommentEntity, Long> {
    List<KnowledgeCommentEntity> findByArticleIdAndStatusOrderByCreatedAtAsc(
            long articleId, KnowledgeCommentEntity.Status status);

    long countByArticleIdAndStatus(long articleId, KnowledgeCommentEntity.Status status);

    long countByStatus(KnowledgeCommentEntity.Status status);
}
