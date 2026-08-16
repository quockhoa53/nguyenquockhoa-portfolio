package com.portfolio.infrastructure.persistence.repository;

import com.portfolio.infrastructure.persistence.entity.AdminAllowedIpEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminAllowedIpJpaRepository extends JpaRepository<AdminAllowedIpEntity, Long> {
    List<AdminAllowedIpEntity> findByAdminId(long adminId);

    void deleteByAdminIdAndDescription(long adminId, String description);

    void deleteByIdAndAdminId(long id, long adminId);

    boolean existsByIpAddress(String ipAddress);

    boolean existsByAdminIdAndIpAddress(long adminId, String ipAddress);
}
