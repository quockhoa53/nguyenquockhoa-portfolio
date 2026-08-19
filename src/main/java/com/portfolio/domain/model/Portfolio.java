package com.portfolio.domain.model;

import java.time.LocalDate;
import java.util.List;

public record Portfolio(Profile profile, List<Skill> skills, List<Experience> experiences, List<Project> projects) {
    public record Profile(
            Long id,
            String fullName,
            String headline,
            String shortBio,
            String bio,
            String email,
            String phone,
            String location,
            String avatarUrl,
            String githubUrl,
            String linkedinUrl,
            String facebookUrl,
            String education) {}

    public record Skill(Long id, String name, String category, int proficiency, int displayOrder) {}

    public record Experience(
            Long id,
            String company,
            String position,
            LocalDate startDate,
            LocalDate endDate,
            String description,
            int displayOrder) {}

    public record Project(
            Long id,
            String title,
            String description,
            String technologies,
            String imageUrl,
            String demoUrl,
            String sourceUrl,
            boolean featured,
            int displayOrder) {}
}
