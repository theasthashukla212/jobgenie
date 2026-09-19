package com.jobgenie.jobgenie_backend.dto;

import java.time.LocalDateTime;

import com.jobgenie.jobgenie_backend.model.Job;

public record JobResponse(Long id, String title, String company, String location, String description,
                          String type, String experience, String salary, String postedBy,
                          LocalDateTime postedAt, LocalDateTime updatedAt) {
    public static JobResponse from(Job job) {
        return new JobResponse(job.getId(), job.getTitle(), job.getCompany(), job.getLocation(),
                job.getDescription(), job.getType(), job.getExperience(), job.getSalary(),
                job.getPostedBy(), job.getPostedAt(), job.getUpdatedAt());
    }
}
