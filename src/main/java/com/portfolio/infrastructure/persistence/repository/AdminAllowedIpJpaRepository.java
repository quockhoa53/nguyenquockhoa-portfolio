package com.portfolio.infrastructure.persistence.repository;

import com.portfolio.infrastructure.persistence.entity.AdminAllowedIpEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminAllowedIpJpaRepository extends JpaRepository<AdminAllowedIpEntity, Long> {
    boolean existsByIpAddress(String ipAddress);

    Optional<AdminAllowedIpEntity> findByIpAddress(String ipAddress);

    void deleteByDescription(String description);
}
