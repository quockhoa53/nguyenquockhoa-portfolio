package com.portfolio.infrastructure.persistence.repository;

import com.portfolio.infrastructure.persistence.entity.ProjectEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectJpaRepository extends JpaRepository<ProjectEntity, Long> {
    List<ProjectEntity> findAllByOrderByFeaturedDescDisplayOrderAscIdAsc();
}
