package com.portfolio.infrastructure.persistence.repository;

import com.portfolio.infrastructure.persistence.entity.ProfileEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileJpaRepository extends JpaRepository<ProfileEntity, Long> {
    Optional<ProfileEntity> findFirstByOrderByIdAsc();
}
