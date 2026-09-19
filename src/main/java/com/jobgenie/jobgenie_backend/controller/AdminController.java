package com.jobgenie.jobgenie_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jobgenie.jobgenie_backend.dto.UserAdminResponse;
import com.jobgenie.jobgenie_backend.exception.ResourceNotFoundException;
import com.jobgenie.jobgenie_backend.model.User;
import com.jobgenie.jobgenie_backend.repository.UserRepository;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<UserAdminResponse>> getUsers() {
        return ResponseEntity.ok(userRepository.findAll().stream().map(UserAdminResponse::from).toList());
    }

    @PatchMapping("/{id}/enabled")
    public ResponseEntity<UserAdminResponse> setEnabled(@PathVariable Long id, @RequestParam boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setEnabled(enabled);
        return ResponseEntity.ok(UserAdminResponse.from(userRepository.save(user)));
    }
}
