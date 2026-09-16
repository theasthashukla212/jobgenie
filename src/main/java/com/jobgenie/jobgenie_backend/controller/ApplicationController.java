package com.jobgenie.jobgenie_backend.controller;

import com.jobgenie.jobgenie_backend.model.Application;
import com.jobgenie.jobgenie_backend.model.User;
import com.jobgenie.jobgenie_backend.repository.ApplicationRepository;
import com.jobgenie.jobgenie_backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"})
public class ApplicationController {

    private final ApplicationRepository applicationRepository;
    private final UserService userService;

    public ApplicationController(
            ApplicationRepository applicationRepository,
            UserService userService
    ) {
        this.applicationRepository = applicationRepository;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<Application>> getUserApplications(@RequestParam Long userId) {
        User user = userService.findById(userId);
        return ResponseEntity.ok(applicationRepository.findByUser(user));
    }

    @PostMapping
    public ResponseEntity<Application> createApplication(@RequestBody Application application) {
        return ResponseEntity.ok(applicationRepository.save(application));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Application> updateApplication(
            @PathVariable Long id,
            @RequestBody Application application
    ) {
        return applicationRepository.findById(id)
                .map(existingApplication -> {
                    existingApplication.setStatus(application.getStatus());
                    existingApplication.setNotes(application.getNotes());
                    return ResponseEntity.ok(applicationRepository.save(existingApplication));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        if (!applicationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        applicationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
