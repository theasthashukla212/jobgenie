package com.jobgenie.jobgenie_backend.dto;

import java.time.LocalDateTime;

import com.jobgenie.jobgenie_backend.model.Resume;

public record ResumeResponse(Long id, String title, String content, String filePath, boolean isDefault,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
    public static ResumeResponse from(Resume resume) {
        return new ResumeResponse(resume.getId(), resume.getTitle(), resume.getContent(), resume.getFilePath(),
                resume.isDefault(), resume.getCreatedAt(), resume.getUpdatedAt());
    }
}
