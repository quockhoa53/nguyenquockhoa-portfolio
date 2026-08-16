package com.portfolio.infrastructure.persistence.repository;

import com.portfolio.infrastructure.persistence.entity.ProjectLikeEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProjectLikeJpaRepository extends JpaRepository<ProjectLikeEntity, Long> {
    long countByProjectId(long projectId);

    Optional<ProjectLikeEntity> findByProjectIdAndGuestId(long projectId, UUID guestId);

    @Query("SELECT l FROM ProjectLikeEntity l JOIN FETCH l.project JOIN FETCH l.guest ORDER BY l.createdAt DESC")
    List<ProjectLikeEntity> findAllWithDetails();
}
