package com.jobgenie.jobgenie_backend.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.jobgenie.jobgenie_backend.dto.ResumeRequest;
import com.jobgenie.jobgenie_backend.dto.ResumeResponse;
import com.jobgenie.jobgenie_backend.exception.ResourceNotFoundException;
import com.jobgenie.jobgenie_backend.model.Resume;
import com.jobgenie.jobgenie_backend.model.User;
import com.jobgenie.jobgenie_backend.repository.ResumeRepository;
import com.jobgenie.jobgenie_backend.service.ResumeFileStorageService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeRepository resumeRepository;
    private final ResumeFileStorageService fileStorageService;
    public ResumeController(ResumeRepository resumeRepository, ResumeFileStorageService fileStorageService) {
        this.resumeRepository = resumeRepository;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public ResponseEntity<List<ResumeResponse>> getUserResumes(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(resumeRepository.findByUser(user).stream().map(ResumeResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeResponse> getResumeById(@PathVariable Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return resumeRepository.findByIdAndUser(id, user).map(resume -> ResponseEntity.ok(ResumeResponse.from(resume)))
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ResumeResponse> createResume(@Valid @RequestBody ResumeRequest request, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Resume resume = new Resume();
        resume.setUser(user);
        copy(request, resume);
        if (request.isDefault()) setDefault(user, resume);
        return ResponseEntity.status(201).body(ResumeResponse.from(resumeRepository.save(resume)));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<ResumeResponse> uploadResume(
            @RequestParam String title,
            @RequestParam(defaultValue = "false") boolean isDefault,
            @RequestPart("file") MultipartFile file,
            Authentication authentication) {
        if (title.isBlank() || title.length() > 150) {
            throw new IllegalArgumentException("Title is required and must be at most 150 characters");
        }
        User user = (User) authentication.getPrincipal();
        Resume resume = new Resume();
        resume.setUser(user);
        resume.setTitle(title.trim());
        resume.setFilePath(fileStorageService.store(user.getId(), file));
        if (isDefault) setDefault(user, resume);
        return ResponseEntity.status(201).body(ResumeResponse.from(resumeRepository.save(resume)));
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> downloadResume(@PathVariable Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Resume resume = resumeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
        if (resume.getFilePath() == null || resume.getFilePath().isBlank()) {
            throw new ResourceNotFoundException("Resume file not found");
        }
        Resource resource = fileStorageService.load(resume.getFilePath());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resume.getTitle().replaceAll("[^A-Za-z0-9._-]", "_") + "\"")
            .contentType(mediaTypeFor(resume.getFilePath()))
                .body(resource);
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ResumeResponse> updateResume(@PathVariable Long id, @Valid @RequestBody ResumeRequest request, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return resumeRepository.findByIdAndUser(id, user)
                .map(existingResume -> {
                    copy(request, existingResume);
                    if (request.isDefault()) setDefault(user, existingResume);
                    existingResume.setUpdatedAt(java.time.LocalDateTime.now());
                    return ResponseEntity.ok(ResumeResponse.from(resumeRepository.save(existingResume)));
                })
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResume(@PathVariable Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Resume resume = resumeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
        resumeRepository.delete(resume);
        return ResponseEntity.noContent().build();
    }

    private void copy(ResumeRequest request, Resume resume) {
        resume.setTitle(request.title().trim());
        resume.setContent(request.content());
        resume.setFilePath(request.filePath());
        resume.setDefault(request.isDefault());
    }

    private void setDefault(User user, Resume selected) {
        resumeRepository.findByUser(user).forEach(resume -> {
            if (!resume.getId().equals(selected.getId())) resume.setDefault(false);
        });
        selected.setDefault(true);
    }

    private MediaType mediaTypeFor(String filePath) {
        String path = filePath.toLowerCase(Locale.ROOT);
        if (path.endsWith(".pdf")) return MediaType.APPLICATION_PDF;
        if (path.endsWith(".txt")) return MediaType.TEXT_PLAIN;
        if (path.endsWith(".doc")) return MediaType.parseMediaType("application/msword");
        if (path.endsWith(".docx")) return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
