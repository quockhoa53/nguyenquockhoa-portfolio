package com.portfolio.infrastructure.persistence.repository;

import com.portfolio.infrastructure.persistence.entity.AdminAllowedIpEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminAllowedIpJpaRepository extends JpaRepository<AdminAllowedIpEntity, Long> {
    void deleteByAdminIdAndDescription(long adminId, String description);

    boolean existsByIpAddress(String ipAddress);

    boolean existsByAdminIdAndIpAddress(long adminId, String ipAddress);
}
