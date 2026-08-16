package com.portfolio.infrastructure.persistence.repository;

import com.portfolio.infrastructure.persistence.entity.SkillEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillJpaRepository extends JpaRepository<SkillEntity, Long> {
    List<SkillEntity> findAllByOrderByDisplayOrderAscIdAsc();
}
