package com.jobgenie.jobgenie_backend.dto;

import com.jobgenie.jobgenie_backend.model.Application;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ApplicationRequest(
        @NotNull(message = "Job id is required") Long jobId,
        Long resumeId,
        Application.Status status,
        @Size(max = 10000, message = "Notes must be at most 10000 characters") String notes
) {}
