package com.portfolio.infrastructure.persistence.repository;

import com.portfolio.infrastructure.persistence.entity.KnowledgeCommentEntity;
import com.portfolio.infrastructure.persistence.entity.ProjectCommentEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProjectCommentJpaRepository extends JpaRepository<ProjectCommentEntity, Long> {
    @Query(
            "SELECT c FROM ProjectCommentEntity c JOIN FETCH c.guest WHERE c.project.id = :projectId AND c.status IN :statuses ORDER BY c.createdAt ASC")
    List<ProjectCommentEntity> findByProjectIdAndStatusInOrderByCreatedAtAsc(
            @org.springframework.data.repository.query.Param("projectId") long projectId,
            @org.springframework.data.repository.query.Param("statuses")
                    java.util.Collection<KnowledgeCommentEntity.Status> statuses);

    @Query("SELECT c FROM ProjectCommentEntity c JOIN FETCH c.guest ORDER BY c.createdAt DESC")
    List<ProjectCommentEntity> findAllWithGuest();

    long countByProjectIdAndStatusIn(long projectId, java.util.Collection<KnowledgeCommentEntity.Status> statuses);

    long countByProjectIdAndStatus(long projectId, KnowledgeCommentEntity.Status status);

    long countByStatus(KnowledgeCommentEntity.Status status);
}
