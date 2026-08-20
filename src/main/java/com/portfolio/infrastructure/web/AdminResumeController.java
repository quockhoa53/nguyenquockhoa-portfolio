package com.portfolio.infrastructure.web;

import com.portfolio.application.service.ResumeService;
import com.portfolio.domain.model.Resume;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/admin/resumes")
public class AdminResumeController {

    private final ResumeService resumeService;

    public AdminResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    public record ResumeRequest(
            @NotBlank String title,
            String targetRole,
            @NotBlank String fileUrl,
            String fileName,
            Long fileSize,
            String summary,
            boolean isPrimary,
            boolean isActive) {}

    @GetMapping
    public List<Resume> getAllResumes() {
        return resumeService.getAllResumesForAdmin();
    }

    @GetMapping("/{id}")
    public Resume getResume(@PathVariable Long id) {
        return resumeService
                .getResumeById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy CV"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Resume createResume(@Valid @RequestBody ResumeRequest request) {
        Resume resume = new Resume(
                null,
                request.title().trim(),
                request.targetRole() != null && !request.targetRole().isBlank()
                        ? request.targetRole().trim().toUpperCase()
                        : "GENERAL",
                request.fileUrl().trim(),
                request.fileName() != null ? request.fileName().trim() : null,
                request.fileSize() != null ? request.fileSize() : 0L,
                request.summary(),
                request.isPrimary(),
                request.isActive(),
                0,
                OffsetDateTime.now(),
                OffsetDateTime.now());
        return resumeService.saveResume(resume);
    }

    @PutMapping("/{id}")
    public Resume updateResume(@PathVariable Long id, @Valid @RequestBody ResumeRequest request) {
        Resume existing = resumeService
                .getResumeById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy CV"));

        Resume updated = new Resume(
                existing.id(),
                request.title().trim(),
                request.targetRole() != null && !request.targetRole().isBlank()
                        ? request.targetRole().trim().toUpperCase()
                        : "GENERAL",
                request.fileUrl().trim(),
                request.fileName() != null ? request.fileName().trim() : existing.fileName(),
                request.fileSize() != null ? request.fileSize() : existing.fileSize(),
                request.summary(),
                request.isPrimary(),
                request.isActive(),
                existing.downloadCount(),
                existing.createdAt(),
                OffsetDateTime.now());

        return resumeService.saveResume(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResume(@PathVariable Long id) {
        resumeService.deleteResume(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/primary")
    public ResponseEntity<Void> setPrimary(@PathVariable Long id) {
        resumeService.setPrimaryResume(id);
        return ResponseEntity.ok().build();
    }
}
