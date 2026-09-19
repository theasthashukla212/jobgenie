package com.jobgenie.jobgenie_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jobgenie.jobgenie_backend.dto.AtsScoreRequest;
import com.jobgenie.jobgenie_backend.dto.AtsScoreResponse;
import com.jobgenie.jobgenie_backend.dto.TailorResumeRequest;
import com.jobgenie.jobgenie_backend.dto.TailorResumeResponse;
import com.jobgenie.jobgenie_backend.service.AiService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/ats-score")
    public ResponseEntity<AtsScoreResponse> score(@Valid @RequestBody AtsScoreRequest request) {
        return ResponseEntity.ok(aiService.score(request.resumeText(), request.jobDescription()));
    }

    @PostMapping("/tailor-resume")
    public ResponseEntity<TailorResumeResponse> tailor(@Valid @RequestBody TailorResumeRequest request) {
        return ResponseEntity.ok(aiService.tailor(request.resumeText(), request.jobDescription()));
    }
}