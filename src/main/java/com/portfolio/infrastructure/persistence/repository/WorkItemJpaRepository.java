package com.portfolio.infrastructure.persistence.repository;

import com.portfolio.infrastructure.persistence.entity.WorkItemEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkItemJpaRepository extends JpaRepository<WorkItemEntity, Long> {
    List<WorkItemEntity> findByPublishedTrueOrderByDisplayOrderAscIdAsc();

    List<WorkItemEntity> findAllByOrderByDisplayOrderAscIdAsc();

    Optional<WorkItemEntity> findBySlugAndPublishedTrue(String slug);
}
