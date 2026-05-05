package com.swer313.projectstep1.user;

import jakarta.validation.constraints.NotNull;

public class UserStatusUpdateRequest {
    @NotNull(message = "active is required")
    private Boolean active;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
