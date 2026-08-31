package com.portfolio.infrastructure.persistence.repository;

import com.portfolio.infrastructure.persistence.entity.KnowledgeCommentEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface KnowledgeCommentJpaRepository extends JpaRepository<KnowledgeCommentEntity, Long> {
    @Query(
            "SELECT c FROM KnowledgeCommentEntity c JOIN FETCH c.guest WHERE c.article.id = :articleId AND c.status IN :statuses ORDER BY c.createdAt ASC")
    List<KnowledgeCommentEntity> findByArticleIdAndStatusInOrderByCreatedAtAsc(
            @org.springframework.data.repository.query.Param("articleId") long articleId,
            @org.springframework.data.repository.query.Param("statuses")
                    java.util.Collection<KnowledgeCommentEntity.Status> statuses);

    @Query("SELECT c FROM KnowledgeCommentEntity c JOIN FETCH c.guest ORDER BY c.createdAt DESC")
    List<KnowledgeCommentEntity> findAllWithGuest();

    long countByArticleIdAndStatusIn(long articleId, java.util.Collection<KnowledgeCommentEntity.Status> statuses);

    long countByArticleIdAndStatus(long articleId, KnowledgeCommentEntity.Status status);

    long countByStatus(KnowledgeCommentEntity.Status status);
}
