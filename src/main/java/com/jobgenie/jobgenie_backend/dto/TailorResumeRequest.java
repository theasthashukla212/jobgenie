package com.jobgenie.jobgenie_backend.dto;

import jakarta.validation.constraints.NotBlank;

public record TailorResumeRequest(@NotBlank String resumeText, @NotBlank String jobDescription) {}