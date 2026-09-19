package com.jobgenie.jobgenie_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jobgenie.jobgenie_backend.dto.ApplicationRequest;
import com.jobgenie.jobgenie_backend.dto.ApplicationResponse;
import com.jobgenie.jobgenie_backend.exception.ResourceNotFoundException;
import com.jobgenie.jobgenie_backend.model.Application;
import com.jobgenie.jobgenie_backend.model.Job;
import com.jobgenie.jobgenie_backend.model.Resume;
import com.jobgenie.jobgenie_backend.model.User;
import com.jobgenie.jobgenie_backend.repository.ApplicationRepository;
import com.jobgenie.jobgenie_backend.repository.JobRepository;
import com.jobgenie.jobgenie_backend.repository.ResumeRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;

    public ApplicationController(
            ApplicationRepository applicationRepository,
            JobRepository jobRepository,
            ResumeRepository resumeRepository
    ) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.resumeRepository = resumeRepository;
    }

    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getUserApplications(
            @RequestParam(required = false) Application.Status status, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<Application> applications = status == null
                ? applicationRepository.findByUserOrderByUpdatedAtDesc(user)
                : applicationRepository.findByUserAndStatusOrderByUpdatedAtDesc(user, status);
        return ResponseEntity.ok(applications.stream().map(ApplicationResponse::from).toList());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ApplicationResponse> createApplication(@Valid @RequestBody ApplicationRequest request,
                                                                  Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Job job = jobRepository.findById(request.jobId()).orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        Resume resume = request.resumeId() == null ? null : resumeRepository.findByIdAndUser(request.resumeId(), user)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
        Application application = new Application();
        application.setUser(user);
        application.setJob(job);
        application.setResume(resume);
        application.setStatus(request.status() == null ? Application.Status.APPLIED : request.status());
        application.setNotes(request.notes());
        return ResponseEntity.status(201).body(ApplicationResponse.from(applicationRepository.save(application)));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ApplicationResponse> updateApplication(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        return applicationRepository.findByIdAndUser(id, user)
                .map(existingApplication -> {
                    if (request.status() != null) existingApplication.setStatus(request.status());
                    existingApplication.setNotes(request.notes());
                    existingApplication.setUpdatedAt(java.time.LocalDateTime.now());
                    return ResponseEntity.ok(ApplicationResponse.from(applicationRepository.save(existingApplication)));
                })
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Application application = applicationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        applicationRepository.delete(application);
        return ResponseEntity.noContent().build();
    }
}
