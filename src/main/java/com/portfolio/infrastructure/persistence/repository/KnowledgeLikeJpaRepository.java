package com.portfolio.infrastructure.persistence.repository;

import com.portfolio.infrastructure.persistence.entity.KnowledgeLikeEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeLikeJpaRepository extends JpaRepository<KnowledgeLikeEntity, Long> {
    long countByArticleId(long articleId);

    Optional<KnowledgeLikeEntity> findByArticleIdAndGuestId(long articleId, UUID guestId);
}
