package com.portfolio.domain.model;

import java.time.OffsetDateTime;

public record Resume(
        Long id,
        String title,
        String targetRole,
        String fileUrl,
        String fileName,
        Long fileSize,
        String summary,
        boolean isPrimary,
        boolean isActive,
        int downloadCount,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {}
