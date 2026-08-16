package com.portfolio.infrastructure.persistence.repository;

import com.portfolio.infrastructure.persistence.entity.KnowledgeLikeEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface KnowledgeLikeJpaRepository extends JpaRepository<KnowledgeLikeEntity, Long> {
    long countByArticleId(long articleId);

    Optional<KnowledgeLikeEntity> findByArticleIdAndGuestId(long articleId, UUID guestId);

    @Query("SELECT l FROM KnowledgeLikeEntity l JOIN FETCH l.article JOIN FETCH l.guest ORDER BY l.createdAt DESC")
    List<KnowledgeLikeEntity> findAllWithDetails();
}
