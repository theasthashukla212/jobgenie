package com.jobgenie.jobgenie_backend.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jobgenie.jobgenie_backend.dto.ResumeResponse;
import com.jobgenie.jobgenie_backend.dto.ResumeVersionRequest;
import com.jobgenie.jobgenie_backend.dto.ResumeVersionResponse;
import com.jobgenie.jobgenie_backend.model.ResumeVersion;
import com.jobgenie.jobgenie_backend.model.User;
import com.jobgenie.jobgenie_backend.service.ResumeFileStorageService;
import com.jobgenie.jobgenie_backend.service.ResumeVersionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/resumes/{resumeId}/versions")
public class ResumeVersionController {
    private final ResumeVersionService versionService;
    private final ResumeFileStorageService fileStorageService;

    public ResumeVersionController(ResumeVersionService versionService, ResumeFileStorageService fileStorageService) {
        this.versionService = versionService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public ResponseEntity<List<ResumeVersionResponse>> list(@PathVariable Long resumeId, Authentication authentication) {
        return ResponseEntity.ok(versionService.list(resumeId, user(authentication)));
    }

    @PostMapping
    public ResponseEntity<ResumeVersionResponse> create(@PathVariable Long resumeId,
                                                          @Valid @RequestBody ResumeVersionRequest request,
                                                          Authentication authentication) {
        return ResponseEntity.status(201).body(versionService.create(resumeId, request, user(authentication)));
    }

    @PostMapping("/{versionId}/restore")
    public ResponseEntity<ResumeResponse> restore(@PathVariable Long resumeId, @PathVariable Long versionId,
                                                   Authentication authentication) {
        return ResponseEntity.ok(versionService.restore(resumeId, versionId, user(authentication)));
    }

    @GetMapping("/{versionId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long resumeId, @PathVariable Long versionId,
                                              Authentication authentication) {
        ResumeVersion version = versionService.get(resumeId, versionId, user(authentication));
        Resource resource;
        MediaType mediaType;
        if (version.getFilePath() != null && !version.getFilePath().isBlank()) {
            resource = fileStorageService.load(version.getFilePath());
            mediaType = mediaTypeFor(version.getFilePath());
        } else {
            byte[] content = (version.getContent() == null ? "" : version.getContent()).getBytes(StandardCharsets.UTF_8);
            resource = new ByteArrayResource(content);
            mediaType = MediaType.TEXT_PLAIN;
        }
        String filename = version.getTitle().replaceAll("[^A-Za-z0-9._-]", "_");
        if (!filename.contains(".")) filename += ".txt";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .body(resource);
    }

    private User user(Authentication authentication) {
        return (User) authentication.getPrincipal();
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
