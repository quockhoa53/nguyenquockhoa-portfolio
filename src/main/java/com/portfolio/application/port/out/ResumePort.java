package com.portfolio.application.port.out;

import com.portfolio.domain.model.Resume;
import java.util.List;
import java.util.Optional;

public interface ResumePort {

    List<Resume> findAllActive();

    List<Resume> findAllAdmin();

    Optional<Resume> findById(Long id);

    Optional<Resume> findPrimary();

    Resume save(Resume resume);

    void deleteById(Long id);

    void incrementDownloadCount(Long id);

    void setPrimary(Long id);
}
