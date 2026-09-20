package com.jobgenie.jobgenie_backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobgenie.jobgenie_backend.dto.ResumeResponse;
import com.jobgenie.jobgenie_backend.dto.ResumeVersionRequest;
import com.jobgenie.jobgenie_backend.dto.ResumeVersionResponse;
import com.jobgenie.jobgenie_backend.exception.ResourceNotFoundException;
import com.jobgenie.jobgenie_backend.model.Resume;
import com.jobgenie.jobgenie_backend.model.ResumeVersion;
import com.jobgenie.jobgenie_backend.model.User;
import com.jobgenie.jobgenie_backend.repository.ResumeRepository;
import com.jobgenie.jobgenie_backend.repository.ResumeVersionRepository;

@Service
public class ResumeVersionService {
    private final ResumeRepository resumeRepository;
    private final ResumeVersionRepository versionRepository;

    public ResumeVersionService(ResumeRepository resumeRepository, ResumeVersionRepository versionRepository) {
        this.resumeRepository = resumeRepository;
        this.versionRepository = versionRepository;
    }

    @Transactional(readOnly = true)
    public List<ResumeVersionResponse> list(Long resumeId, User user) {
        requireResume(resumeId, user);
        return versionRepository.findByResumeIdAndResumeUserOrderByVersionNumberDesc(resumeId, user)
                .stream().map(ResumeVersionResponse::from).toList();
    }

    @Transactional
    public ResumeVersionResponse create(Long resumeId, ResumeVersionRequest request, User user) {
        Resume resume = requireResume(resumeId, user);
        ResumeVersion version = new ResumeVersion();
        version.setResume(resume);
        version.setVersionNumber(versionRepository.countByResumeId(resumeId) + 1);
        version.setTitle(valueOrDefault(request.title(), resume.getTitle()));
        version.setContent(request.content() == null ? resume.getContent() : request.content());
        version.setFilePath(request.filePath() == null ? resume.getFilePath() : request.filePath());
        return ResumeVersionResponse.from(versionRepository.save(version));
    }

    @Transactional
    public ResumeResponse restore(Long resumeId, Long versionId, User user) {
        Resume resume = requireResume(resumeId, user);
        ResumeVersion version = versionRepository.findByIdAndResumeIdAndResumeUser(versionId, resumeId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Resume version not found"));
        resume.setTitle(version.getTitle());
        resume.setContent(version.getContent());
        resume.setFilePath(version.getFilePath());
        resume.setUpdatedAt(LocalDateTime.now());
        return ResumeResponse.from(resumeRepository.save(resume));
    }

    @Transactional(readOnly = true)
    public ResumeVersion get(Long resumeId, Long versionId, User user) {
        requireResume(resumeId, user);
        return versionRepository.findByIdAndResumeIdAndResumeUser(versionId, resumeId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Resume version not found"));
    }

    private Resume requireResume(Long resumeId, User user) {
        return resumeRepository.findByIdAndUser(resumeId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
