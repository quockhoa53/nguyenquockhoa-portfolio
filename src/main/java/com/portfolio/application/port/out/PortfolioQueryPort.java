package com.portfolio.application.port.out;

import com.portfolio.domain.model.Portfolio;
import java.util.List;
import java.util.Optional;

public interface PortfolioQueryPort {
    Portfolio load();

    Portfolio.Profile loadProfile();

    List<Portfolio.Skill> loadSkills();

    List<Portfolio.Experience> loadExperiences();

    List<Portfolio.Project> loadProjects();

    Optional<Portfolio.Project> loadProject(long id);
}
