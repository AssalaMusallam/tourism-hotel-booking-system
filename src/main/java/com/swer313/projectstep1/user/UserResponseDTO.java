package com.swer313.projectstep1.user;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "User response data")
public class UserResponseDTO {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "Full name", example = "Test User")
    private String fullName;

    @Schema(description = "Email", example = "test@example.com")
    private String email;

    @Schema(description = "Phone number", example = "0591234567")
    private String phone;

    @Schema(description = "User role", example = "GUEST")
    private UserRole role;

    @Schema(description = "Account active status", example = "true")
    private boolean active;

    @Schema(description = "Account creation time", example = "2026-04-07T10:30:00Z")
    private Instant createdAt;

    private Long managedHotelId;

    public UserResponseDTO() {}

    public UserResponseDTO(User u) {
        this.id = u.getId();
        this.fullName = u.getFullName();
        this.email = u.getEmail();
        this.phone = u.getPhone();
        this.role = u.getRole();
        this.active = u.isActive();
        this.createdAt = u.getCreatedAt();
        this.managedHotelId = (u.getManagedHotels() != null
                && !u.getManagedHotels().isEmpty())
                ? u.getManagedHotels().iterator().next().getId()
                : null;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public UserRole getRole() { return role; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Long getManagedHotelId() { return managedHotelId; }
}
