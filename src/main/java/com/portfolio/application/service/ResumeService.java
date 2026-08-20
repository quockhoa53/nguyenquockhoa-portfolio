package com.portfolio.application.service;

import com.portfolio.application.port.out.ResumePort;
import com.portfolio.domain.model.Resume;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResumeService {

    private final ResumePort resumePort;

    public ResumeService(ResumePort resumePort) {
        this.resumePort = resumePort;
    }

    public List<Resume> getActiveResumes() {
        return resumePort.findAllActive();
    }

    public List<Resume> getAllResumesForAdmin() {
        return resumePort.findAllAdmin();
    }

    public Optional<Resume> getResumeById(Long id) {
        return resumePort.findById(id);
    }

    public Optional<Resume> getPrimaryResume() {
        return resumePort.findPrimary();
    }

    @Transactional
    public Resume saveResume(Resume resume) {
        return resumePort.save(resume);
    }

    @Transactional
    public void deleteResume(Long id) {
        resumePort.deleteById(id);
    }

    @Transactional
    public void incrementDownload(Long id) {
        resumePort.incrementDownloadCount(id);
    }

    @Transactional
    public void setPrimaryResume(Long id) {
        resumePort.setPrimary(id);
    }
}
