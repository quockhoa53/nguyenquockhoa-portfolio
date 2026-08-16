package com.portfolio.domain.model;

import java.time.OffsetDateTime;

public record ContactMessage(
        Long id, String name, String email, String subject, String message, OffsetDateTime createdAt) {}
