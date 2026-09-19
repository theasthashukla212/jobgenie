package com.jobgenie.jobgenie_backend.dto;

import java.time.LocalDateTime;

import com.jobgenie.jobgenie_backend.model.User;

public record UserAdminResponse(Long id, String email, String firstName, String lastName,
                                String phone, User.Role role, boolean enabled, LocalDateTime createdAt) {
    public static UserAdminResponse from(User user) {
        return new UserAdminResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
                user.getPhone(), user.getRole(), user.isEnabledFlag(), user.getCreatedAt());
    }
}
