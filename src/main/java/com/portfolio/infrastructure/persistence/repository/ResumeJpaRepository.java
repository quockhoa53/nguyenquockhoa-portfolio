package com.portfolio.infrastructure.persistence.repository;

import com.portfolio.infrastructure.persistence.entity.ResumeEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeJpaRepository extends JpaRepository<ResumeEntity, Long> {

    List<ResumeEntity> findAllByIsActiveTrueOrderByIsPrimaryDescUpdatedAtDesc();

    List<ResumeEntity> findAllByOrderByIsPrimaryDescUpdatedAtDesc();

    Optional<ResumeEntity> findFirstByIsPrimaryTrueAndIsActiveTrue();

    Optional<ResumeEntity> findFirstByTargetRoleIgnoreCaseAndIsActiveTrue(String targetRole);

    @Modifying
    @Query("UPDATE ResumeEntity r SET r.isPrimary = false WHERE r.id != :id")
    void resetOtherPrimaryResumes(Long id);

    @Modifying
    @Query("UPDATE ResumeEntity r SET r.downloadCount = r.downloadCount + 1 WHERE r.id = :id")
    void incrementDownloadCount(Long id);
}
