package com.portfolio.application.service;

import com.portfolio.application.port.in.GetPortfolioUseCase;
import com.portfolio.application.port.out.PortfolioQueryPort;
import com.portfolio.domain.model.Portfolio;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortfolioService implements GetPortfolioUseCase {

    private final PortfolioQueryPort queryPort;

    public PortfolioService(PortfolioQueryPort queryPort) {
        this.queryPort = queryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public Portfolio getPortfolio() {
        return queryPort.load();
    }

    @Override
    @Transactional(readOnly = true)
    public Portfolio.Profile getProfile() {
        return queryPort.loadProfile();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Portfolio.Skill> getSkills() {
        return queryPort.loadSkills();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Portfolio.Experience> getExperiences() {
        return queryPort.loadExperiences();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Portfolio.Project> getProjects() {
        return queryPort.loadProjects();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Portfolio.Project> getProject(long id) {
        return queryPort.loadProject(id);
    }
}
