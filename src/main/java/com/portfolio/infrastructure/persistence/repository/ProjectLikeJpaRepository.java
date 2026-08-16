package com.portfolio.infrastructure.persistence.repository;

import com.portfolio.infrastructure.persistence.entity.ProjectLikeEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectLikeJpaRepository extends JpaRepository<ProjectLikeEntity, Long> {
    long countByProjectId(long projectId);

    Optional<ProjectLikeEntity> findByProjectIdAndGuestId(long projectId, UUID guestId);
}
