package com.jobgenie.jobgenie_backend.controller;

import com.jobgenie.jobgenie_backend.model.Resume;
import com.jobgenie.jobgenie_backend.model.User;
import com.jobgenie.jobgenie_backend.repository.ResumeRepository;
import com.jobgenie.jobgenie_backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"})
public class ResumeController {

    private final ResumeRepository resumeRepository;
    private final UserService userService;

    public ResumeController(ResumeRepository resumeRepository, UserService userService) {
        this.resumeRepository = resumeRepository;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<Resume>> getUserResumes(@RequestParam Long userId) {
        User user = userService.findById(userId);
        List<Resume> resumes = resumeRepository.findByUser(user);
        return ResponseEntity.ok(resumes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resume> getResumeById(@PathVariable Long id) {
        return resumeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Resume> createResume(@RequestBody Resume resume) {
        Resume savedResume = resumeRepository.save(resume);
        return ResponseEntity.ok(savedResume);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Resume> updateResume(@PathVariable Long id, @RequestBody Resume resume) {
        return resumeRepository.findById(id)
                .map(existingResume -> {
                    existingResume.setTitle(resume.getTitle());
                    existingResume.setContent(resume.getContent());
                    existingResume.setFilePath(resume.getFilePath());
                    existingResume.setDefault(resume.isDefault());
                    return ResponseEntity.ok(resumeRepository.save(existingResume));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResume(@PathVariable Long id) {
        if (resumeRepository.existsById(id)) {
            resumeRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
