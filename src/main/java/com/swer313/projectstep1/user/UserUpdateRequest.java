package com.swer313.projectstep1.user;

import jakarta.validation.constraints.Size;

public class UserUpdateRequest {

    @Size(max = 150, message = "fullName must be at most 150 characters")
    private String fullName;

    @Size(max = 30, message = "phone must be at most 30 characters")
    private String phone;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
