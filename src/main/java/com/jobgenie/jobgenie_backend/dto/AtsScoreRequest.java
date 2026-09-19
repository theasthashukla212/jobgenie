package com.jobgenie.jobgenie_backend.dto;

import jakarta.validation.constraints.NotBlank;

public record AtsScoreRequest(@NotBlank String resumeText, @NotBlank String jobDescription) {}