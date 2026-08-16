package com.portfolio.infrastructure.persistence.repository;

import com.portfolio.infrastructure.persistence.entity.ContactMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactMessageJpaRepository extends JpaRepository<ContactMessageEntity, Long> {}
