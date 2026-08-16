package com.portfolio.infrastructure.persistence.repository;

import com.portfolio.infrastructure.persistence.entity.KnowledgeCategoryEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeCategoryJpaRepository extends JpaRepository<KnowledgeCategoryEntity, Long> {
    List<KnowledgeCategoryEntity> findAllByOrderByDisplayOrderAscIdAsc();

    Optional<KnowledgeCategoryEntity> findBySlug(String slug);
}
