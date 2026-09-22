package com.jobgenie.jobgenie_backend.controller;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jobgenie.jobgenie_backend.dto.GitHubProfileDto;
import com.jobgenie.jobgenie_backend.model.User;
import com.jobgenie.jobgenie_backend.service.GitHubService;

@RestController
@RequestMapping("/api/github")
public class GitHubController {

    private final GitHubService gitHubService;

    public GitHubController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @GetMapping("/connect")
    public ResponseEntity<Map<String, String>> connect(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        String connectUrl = gitHubService.generateConnectUrl(user);
        return ResponseEntity.ok(Map.of("url", connectUrl));
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "state", required = false) String state,
            @RequestParam(name = "error", required = false) String error
    ) {
        if (error != null && !error.isBlank()) {
            String redirectUrl = gitHubService.handleOAuthCallback(null, state);
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(redirectUrl));
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }

        String redirectUrl = gitHubService.handleOAuthCallback(code, state);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(redirectUrl));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/profile")
    public ResponseEntity<GitHubProfileDto> getProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        GitHubProfileDto profile = gitHubService.getProfile(user);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/repositories")
    public ResponseEntity<?> getRepositories(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        GitHubProfileDto profile = gitHubService.getProfile(user);
        return ResponseEntity.ok(profile.getRepos() != null ? profile.getRepos() : Collections.emptyList());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        GitHubProfileDto profile = gitHubService.getProfile(user);
        return ResponseEntity.ok(Map.of(
                "connected", profile.isConnected(),
                "publicRepos", profile.getPublicRepos() != null ? profile.getPublicRepos() : 0,
                "totalStars", profile.getTotalStars() != null ? profile.getTotalStars() : 0,
                "totalForks", profile.getTotalForks() != null ? profile.getTotalForks() : 0,
                "contributions", profile.getContributions() != null ? profile.getContributions() : 0
        ));
    }

    @PostMapping("/disconnect")
    public ResponseEntity<Map<String, String>> disconnect(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        gitHubService.disconnect(user);
        return ResponseEntity.ok(Map.of("message", "GitHub account disconnected successfully."));
    }
}
