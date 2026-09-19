package com.jobgenie.jobgenie_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jobgenie.jobgenie_backend.config.JwtService;
import com.jobgenie.jobgenie_backend.dto.AuthRequest;
import com.jobgenie.jobgenie_backend.dto.AuthResponse;
import com.jobgenie.jobgenie_backend.dto.RefreshRequest;
import com.jobgenie.jobgenie_backend.dto.RegisterRequest;
import com.jobgenie.jobgenie_backend.model.User;
import com.jobgenie.jobgenie_backend.service.RefreshTokenService;
import com.jobgenie.jobgenie_backend.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthenticationController(AuthenticationManager authenticationManager, UserService userService,
                                    JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.registerUser(
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                request.getPhone()
        );

        String token = jwtService.generateToken(user);
        return ResponseEntity.status(201).body(toResponse(token, user, refreshTokenService.issue(user)));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = userService.loadUserByUsername(request.getEmail());
        User user = userService.findByEmail(request.getEmail());
        String token = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(toResponse(token, user, refreshTokenService.issue(user)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        User user = refreshTokenService.consume(request.refreshToken());
        String accessToken = jwtService.generateToken(user);
        return ResponseEntity.ok(toResponse(accessToken, user, refreshTokenService.issue(user)));
    }

    private AuthResponse toResponse(String token, User user, RefreshTokenService.IssuedRefreshToken refreshToken) {
        return new AuthResponse(token, user.getEmail(), user.getFirstName(), user.getLastName(),
                user.getId(), user.getRole().name(), refreshToken.value(), refreshToken.expiresInSeconds());
    }
}
