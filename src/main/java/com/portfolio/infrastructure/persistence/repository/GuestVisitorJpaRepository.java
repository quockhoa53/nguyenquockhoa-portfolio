package com.portfolio.infrastructure.persistence.repository;

import com.portfolio.infrastructure.persistence.entity.GuestVisitorEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestVisitorJpaRepository extends JpaRepository<GuestVisitorEntity, UUID> {}
