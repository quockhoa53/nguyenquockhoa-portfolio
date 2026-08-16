package com.portfolio.infrastructure.persistence.repository;

import com.portfolio.infrastructure.persistence.entity.KnowledgeCommentEntity;
import com.portfolio.infrastructure.persistence.entity.ProjectCommentEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectCommentJpaRepository extends JpaRepository<ProjectCommentEntity, Long> {
    List<ProjectCommentEntity> findByProjectIdAndStatusOrderByCreatedAtAsc(
            long projectId, KnowledgeCommentEntity.Status status);

    long countByProjectIdAndStatus(long projectId, KnowledgeCommentEntity.Status status);

    long countByStatus(KnowledgeCommentEntity.Status status);
}
