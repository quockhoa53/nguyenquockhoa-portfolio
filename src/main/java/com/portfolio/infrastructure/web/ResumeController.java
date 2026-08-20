package com.portfolio.infrastructure.web;

import com.portfolio.application.service.ResumeService;
import com.portfolio.domain.model.Resume;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @GetMapping
    public List<Resume> getActiveResumes() {
        return resumeService.getActiveResumes();
    }

    @GetMapping("/primary")
    public Resume getPrimaryResume() {
        return resumeService
                .getPrimaryResume()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy CV chính"));
    }

    @PostMapping("/{id}/download")
    public ResponseEntity<Resume> recordDownload(@PathVariable Long id) {
        Resume resume = resumeService
                .getResumeById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy CV với ID " + id));
        resumeService.incrementDownload(id);
        return ResponseEntity.ok(resume);
    }
}
