package com.portfolio.infrastructure.persistence.repository;

import com.portfolio.infrastructure.persistence.entity.AiFactEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAiFactRepository extends JpaRepository<AiFactEntity, Long> {
    List<AiFactEntity> findAllByOrderByDisplayOrderAscIdAsc();
    List<AiFactEntity> findAllByIsActiveTrueOrderByDisplayOrderAscIdAsc();
}
