package com.jobgenie.jobgenie_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JobRequest(
        @NotBlank(message = "Title is required") @Size(max = 150, message = "Title must be at most 150 characters") String title,
        @NotBlank(message = "Company is required") @Size(max = 150, message = "Company must be at most 150 characters") String company,
        @NotBlank(message = "Location is required") @Size(max = 150, message = "Location must be at most 150 characters") String location,
        @Size(max = 10000, message = "Description must be at most 10000 characters") String description,
        @Size(max = 50, message = "Type must be at most 50 characters") String type,
        @Size(max = 100, message = "Experience must be at most 100 characters") String experience,
        @Size(max = 100, message = "Salary must be at most 100 characters") String salary
) {}
