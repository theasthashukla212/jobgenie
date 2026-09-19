package com.jobgenie.jobgenie_backend.dto;

import java.time.LocalDateTime;

import com.jobgenie.jobgenie_backend.model.Application;

public record ApplicationResponse(Long id, Long jobId, Long resumeId, Application.Status status,
                                  String notes, LocalDateTime appliedAt, LocalDateTime updatedAt) {
    public static ApplicationResponse from(Application application) {
        return new ApplicationResponse(application.getId(), application.getJob().getId(),
                application.getResume() == null ? null : application.getResume().getId(),
                application.getStatus(), application.getNotes(), application.getAppliedAt(), application.getUpdatedAt());
    }
}
