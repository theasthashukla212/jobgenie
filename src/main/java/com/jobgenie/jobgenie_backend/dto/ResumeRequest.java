package com.jobgenie.jobgenie_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResumeRequest(
        @NotBlank(message = "Title is required") @Size(max = 150, message = "Title must be at most 150 characters") String title,
        @Size(max = 50000, message = "Content must be at most 50000 characters") String content,
        @Size(max = 500, message = "File path must be at most 500 characters") String filePath,
        boolean isDefault
) {}
