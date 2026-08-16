package com.portfolio.application.port.in;

import com.portfolio.domain.model.Portfolio;
import java.util.List;
import java.util.Optional;

public interface GetPortfolioUseCase {
    Portfolio getPortfolio();

    Portfolio.Profile getProfile();

    List<Portfolio.Skill> getSkills();

    List<Portfolio.Experience> getExperiences();

    List<Portfolio.Project> getProjects();

    Optional<Portfolio.Project> getProject(long id);
}
