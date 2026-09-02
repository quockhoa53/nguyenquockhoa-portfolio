package com.portfolio.infrastructure.persistence;

import com.portfolio.application.port.out.PortfolioQueryPort;
import com.portfolio.domain.model.Portfolio;
import com.portfolio.infrastructure.persistence.entity.*;
import com.portfolio.infrastructure.persistence.repository.*;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresPortfolioAdapter implements PortfolioQueryPort {
    private final ProfileJpaRepository profiles;
    private final SkillJpaRepository skills;
    private final ExperienceJpaRepository experiences;
    private final ProjectJpaRepository projects;

    public PostgresPortfolioAdapter(
            ProfileJpaRepository profiles,
            SkillJpaRepository skills,
            ExperienceJpaRepository experiences,
            ProjectJpaRepository projects) {
        this.profiles = profiles;
        this.skills = skills;
        this.experiences = experiences;
        this.projects = projects;
    }

    @Override
    public Portfolio load() {
        return new Portfolio(loadProfile(), loadSkills(), loadExperiences(), loadProjects());
    }

    @Override
    public Portfolio.Profile loadProfile() {
        return profiles.findFirstByIsPublishedTrueOrderByIdDesc()
                .or(() -> profiles.findFirstByOrderByIdAsc())
                .map(this::toDomain)
                .orElseGet(
                        () -> new Portfolio.Profile(
                                1L,
                                "Nguyễn Quốc Khoa",
                                "Software Engineer",
                                "Software Engineer",
                                "<p>Software Engineer Bio</p>",
                                "hello@example.com",
                                "0969895549",
                                "Đồng Tháp, Việt Nam",
                                "/images/hero_3d_developer_character.png",
                                "https://github.com",
                                "https://linkedin.com",
                                "https://facebook.com",
                                "{\"school\": \"Học viện Công nghệ Bưu chính Viễn thông (PTIT)\", \"major\": \"Công nghệ Thông tin\", \"degree\": \"Kỹ sư\", \"period\": \"2020 — 2024\"}"));
    }

    @Override
    public List<Portfolio.Skill> loadSkills() {
        return skills.findAllByOrderByDisplayOrderAscIdAsc().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Portfolio.Experience> loadExperiences() {
        return experiences.findAllByOrderByDisplayOrderAscStartDateDesc().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Portfolio.Project> loadProjects() {
        return projects.findAllByOrderByFeaturedDescDisplayOrderAscIdAsc().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Portfolio.Project> loadProject(long id) {
        return projects.findById(id).map(this::toDomain);
    }

    private Portfolio.Profile toDomain(ProfileEntity e) {
        return new Portfolio.Profile(
                e.getId(),
                e.getFullName(),
                e.getHeadline(),
                e.getShortBio(),
                e.getBio(),
                e.getEmail(),
                e.getPhone(),
                e.getLocation(),
                e.getAvatarUrl(),
                e.getGithubUrl(),
                e.getLinkedinUrl(),
                e.getFacebookUrl(),
                e.getEducation());
    }

    private Portfolio.Skill toDomain(SkillEntity e) {
        return new Portfolio.Skill(e.getId(), e.getName(), e.getCategory(), e.getProficiency(), e.getDisplayOrder());
    }

    private Portfolio.Experience toDomain(ExperienceEntity e) {
        return new Portfolio.Experience(
                e.getId(),
                e.getCompany(),
                e.getPosition(),
                e.getStartDate(),
                e.getEndDate(),
                e.getDescription(),
                e.getDisplayOrder());
    }

    private Portfolio.Project toDomain(ProjectEntity e) {
        return new Portfolio.Project(
                e.getId(),
                e.getTitle(),
                e.getDescription(),
                e.getTechnologies(),
                e.getImageUrl(),
                e.getDemoUrl(),
                e.getSourceUrl(),
                e.isFeatured(),
                e.getDisplayOrder());
    }
}
