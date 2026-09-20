package com.jobgenie.jobgenie_backend.dto;

import java.time.LocalDateTime;

import com.jobgenie.jobgenie_backend.model.ResumeVersion;

public record ResumeVersionResponse(Long id, Long resumeId, int versionNumber, String title,
                                    String content, String filePath, LocalDateTime createdAt,
                                    LocalDateTime updatedAt) {
    public static ResumeVersionResponse from(ResumeVersion version) {
        return new ResumeVersionResponse(version.getId(), version.getResume().getId(), version.getVersionNumber(),
                version.getTitle(), version.getContent(), version.getFilePath(), version.getCreatedAt(), version.getUpdatedAt());
    }
}
