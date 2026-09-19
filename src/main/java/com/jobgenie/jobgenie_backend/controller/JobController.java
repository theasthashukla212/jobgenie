package com.jobgenie.jobgenie_backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jobgenie.jobgenie_backend.dto.ExternalJobResponse;
import com.jobgenie.jobgenie_backend.dto.JobRequest;
import com.jobgenie.jobgenie_backend.dto.JobResponse;
import com.jobgenie.jobgenie_backend.dto.PageResponse;
import com.jobgenie.jobgenie_backend.exception.ResourceNotFoundException;
import com.jobgenie.jobgenie_backend.model.Job;
import com.jobgenie.jobgenie_backend.repository.JobRepository;
import com.jobgenie.jobgenie_backend.service.ExternalJobService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobRepository jobRepository;
    private final ExternalJobService externalJobService;

    public JobController(JobRepository jobRepository, ExternalJobService externalJobService) {
        this.jobRepository = jobRepository;
        this.externalJobService = externalJobService;
    }

    @GetMapping("/external")
    public ResponseEntity<java.util.List<ExternalJobResponse>> getExternalJobs(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(externalJobService.search(search));
    }

    @GetMapping
    public ResponseEntity<PageResponse<JobResponse>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Page must be non-negative and size must be between 1 and 100");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "postedAt").and(Sort.by("id")));
        Page<Job> jobs = search != null && !search.isBlank()
                ? jobRepository.searchJobs(search.trim(), type, pageable)
                : type != null && !type.isBlank()
                ? jobRepository.findByTypeIgnoreCase(type.trim(), pageable)
                : jobRepository.findAll(pageable);
        return ResponseEntity.ok(PageResponse.from(jobs.map(JobResponse::from)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJobById(@PathVariable Long id) {
        return jobRepository.findById(id)
                .map(job -> ResponseEntity.ok(JobResponse.from(job)))
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody JobRequest request, Authentication authentication) {
        Job job = new Job();
        copy(request, job);
        job.setPostedBy(authentication.getName());
        return ResponseEntity.status(201).body(JobResponse.from(jobRepository.save(job)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ResponseEntity<JobResponse> updateJob(@PathVariable Long id, @Valid @RequestBody JobRequest request) {
        return jobRepository.findById(id)
                .map(existingJob -> {
                    copy(request, existingJob);
                    existingJob.setUpdatedAt(java.time.LocalDateTime.now());
                    return ResponseEntity.ok(JobResponse.from(jobRepository.save(existingJob)));
                })
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        if (jobRepository.existsById(id)) {
            jobRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        throw new ResourceNotFoundException("Job not found");
    }

    private void copy(JobRequest request, Job job) {
        job.setTitle(request.title().trim());
        job.setCompany(request.company().trim());
        job.setLocation(request.location().trim());
        job.setDescription(request.description());
        job.setType(request.type() == null || request.type().isBlank() ? "Full-time" : request.type().trim());
        job.setExperience(request.experience() == null || request.experience().isBlank() ? "Entry Level" : request.experience().trim());
        job.setSalary(request.salary());
    }
}
