package com.jobgenie.jobgenie_backend.dto;

public class AuthResponse {

    private String token;
    private String email;
    private String firstName;
    private String lastName;
    private Long id;
    private String role;
    private String refreshToken;
    private long refreshTokenExpiresIn;

    public AuthResponse(
            String token,
            String email,
            String firstName,
            String lastName,
            Long id,
            String role,
            String refreshToken,
            long refreshTokenExpiresIn
    ) {
        this.token = token;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
        this.role = role;
        this.refreshToken = refreshToken;
        this.refreshTokenExpiresIn = refreshTokenExpiresIn;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public long getRefreshTokenExpiresIn() { return refreshTokenExpiresIn; }
    public void setRefreshTokenExpiresIn(long refreshTokenExpiresIn) { this.refreshTokenExpiresIn = refreshTokenExpiresIn; }
}