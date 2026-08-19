package com.portfolio.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "profiles")
public class ProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false)
    private String headline;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String bio;

    @Column(name = "short_bio", nullable = false, columnDefinition = "TEXT")
    private String shortBio;

    @Column(nullable = false)
    private String email;

    @Column(length = 50)
    private String phone;

    private String location;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @Column(name = "github_url", columnDefinition = "TEXT")
    private String githubUrl;

    @Column(name = "linkedin_url", columnDefinition = "TEXT")
    private String linkedinUrl;

    @Column(name = "facebook_url", columnDefinition = "TEXT")
    private String facebookUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "education", columnDefinition = "jsonb")
    private String education;

    protected ProfileEntity() {}

    public void update(
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
            String education) {
        this.fullName = fullName;
        this.headline = headline;
        this.shortBio = shortBio;
        this.bio = bio;
        this.email = email;
        this.phone = phone;
        this.location = location;
        this.avatarUrl = avatarUrl;
        this.githubUrl = githubUrl;
        this.linkedinUrl = linkedinUrl;
        this.facebookUrl = facebookUrl;
        if (education != null) {
            this.education = education;
        }
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getHeadline() {
        return headline;
    }

    public String getBio() {
        return bio;
    }

    public String getShortBio() {
        return shortBio;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getLocation() {
        return location;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getGithubUrl() {
        return githubUrl;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public String getFacebookUrl() {
        return facebookUrl;
    }

    public String getEducation() {
        return education;
    }
}
