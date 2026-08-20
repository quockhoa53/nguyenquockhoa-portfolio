package com.portfolio.infrastructure.persistence;

import com.portfolio.application.port.out.ResumePort;
import com.portfolio.domain.model.Resume;
import com.portfolio.infrastructure.persistence.entity.ResumeEntity;
import com.portfolio.infrastructure.persistence.repository.ResumeJpaRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PostgresResumeAdapter implements ResumePort {

    private final ResumeJpaRepository repository;

    public PostgresResumeAdapter(ResumeJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Resume> findAllActive() {
        return repository.findAllByIsActiveTrueOrderByIsPrimaryDescUpdatedAtDesc().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Resume> findAllAdmin() {
        return repository.findAllByOrderByIsPrimaryDescUpdatedAtDesc().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Resume> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Resume> findPrimary() {
        return repository.findFirstByIsPrimaryTrueAndIsActiveTrue().map(this::toDomain);
    }

    @Override
    @Transactional
    public Resume save(Resume resume) {
        ResumeEntity entity;
        if (resume.id() != null) {
            entity = repository.findById(resume.id()).orElse(new ResumeEntity());
            entity.setTitle(resume.title());
            entity.setTargetRole(resume.targetRole());
            entity.setFileUrl(resume.fileUrl());
            entity.setFileName(resume.fileName());
            entity.setFileSize(resume.fileSize());
            entity.setSummary(resume.summary());
            entity.setPrimary(resume.isPrimary());
            entity.setActive(resume.isActive());
            entity.setUpdatedAt(OffsetDateTime.now());
        } else {
            entity = new ResumeEntity(
                    null,
                    resume.title(),
                    resume.targetRole(),
                    resume.fileUrl(),
                    resume.fileName(),
                    resume.fileSize(),
                    resume.summary(),
                    resume.isPrimary(),
                    resume.isActive(),
                    0,
                    OffsetDateTime.now(),
                    OffsetDateTime.now());
        }

        ResumeEntity saved = repository.save(entity);
        if (saved.isPrimary()) {
            repository.resetOtherPrimaryResumes(saved.getId());
        }
        return toDomain(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public void incrementDownloadCount(Long id) {
        repository.incrementDownloadCount(id);
    }

    @Override
    @Transactional
    public void setPrimary(Long id) {
        repository.findById(id).ifPresent(entity -> {
            entity.setPrimary(true);
            entity.setUpdatedAt(OffsetDateTime.now());
            repository.save(entity);
            repository.resetOtherPrimaryResumes(id);
        });
    }

    private Resume toDomain(ResumeEntity entity) {
        return new Resume(
                entity.getId(),
                entity.getTitle(),
                entity.getTargetRole(),
                entity.getFileUrl(),
                entity.getFileName(),
                entity.getFileSize(),
                entity.getSummary(),
                entity.isPrimary(),
                entity.isActive(),
                entity.getDownloadCount(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
