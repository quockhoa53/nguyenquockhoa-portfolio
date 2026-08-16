package com.portfolio.infrastructure.persistence.repository;

import com.portfolio.infrastructure.persistence.entity.ExperienceEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExperienceJpaRepository extends JpaRepository<ExperienceEntity, Long> {
    List<ExperienceEntity> findAllByOrderByDisplayOrderAscStartDateDesc();
}
