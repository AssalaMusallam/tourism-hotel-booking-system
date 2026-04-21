package com.swer313.projectstep1.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "Login request data")
public class LoginRequest {
    @Schema(description = "User email", example = "test@example.com")

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    private String email;
    @Schema(description = "User password", example = "Secret123")

    @NotBlank(message = "password is required")
    private String password;

    // Getters / Setters
    public String getEmail()            { return email; }
    public void   setEmail(String v)    { this.email = v; }
    public String getPassword()         { return password; }
    public void   setPassword(String v) { this.password = v; }
}