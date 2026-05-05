package com.swer313.projectstep1.user;

import jakarta.validation.constraints.NotNull;

public class UserRoleUpdateRequest {
    @NotNull(message = "role is required")
    private UserRole role;

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
