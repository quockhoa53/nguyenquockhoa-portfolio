package com.portfolio.infrastructure.persistence.repository;

import com.portfolio.infrastructure.persistence.entity.KnowledgeCommentEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface KnowledgeCommentJpaRepository extends JpaRepository<KnowledgeCommentEntity, Long> {
    List<KnowledgeCommentEntity> findByArticleIdAndStatusOrderByCreatedAtAsc(
            long articleId, KnowledgeCommentEntity.Status status);

    @Query("SELECT c FROM KnowledgeCommentEntity c JOIN FETCH c.guest ORDER BY c.createdAt DESC")
    List<KnowledgeCommentEntity> findAllWithGuest();

    long countByArticleIdAndStatus(long articleId, KnowledgeCommentEntity.Status status);

    long countByStatus(KnowledgeCommentEntity.Status status);
}
