package com.jobgenie.jobgenie_backend.controller;

import com.jobgenie.jobgenie_backend.model.Job;
import com.jobgenie.jobgenie_backend.repository.JobRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"})
public class JobController {

    private final JobRepository jobRepository;

    public JobController(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @GetMapping
    public ResponseEntity<Page<Job>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        
        if (search != null && !search.isEmpty()) {
            return ResponseEntity.ok(jobRepository.searchJobs(search, pageable));
        }
        
        return ResponseEntity.ok(jobRepository.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable Long id) {
        return jobRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Job> createJob(@RequestBody Job job) {
        Job savedJob = jobRepository.save(job);
        return ResponseEntity.ok(savedJob);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Job> updateJob(@PathVariable Long id, @RequestBody Job job) {
        return jobRepository.findById(id)
                .map(existingJob -> {
                    existingJob.setTitle(job.getTitle());
                    existingJob.setCompany(job.getCompany());
                    existingJob.setLocation(job.getLocation());
                    existingJob.setDescription(job.getDescription());
                    existingJob.setType(job.getType());
                    existingJob.setExperience(job.getExperience());
                    existingJob.setSalary(job.getSalary());
                    return ResponseEntity.ok(jobRepository.save(existingJob));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        if (jobRepository.existsById(id)) {
            jobRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
