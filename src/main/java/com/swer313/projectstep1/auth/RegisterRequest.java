// ══════════════════════════════════════════════════════════════════
// FILE 1:  auth/RegisterRequest.java
// ══════════════════════════════════════════════════════════════════
package com.swer313.projectstep1.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
@Schema(description = "Register request data")
public class RegisterRequest {
    @Schema(description = "Full name", example = "Test User")

    @NotBlank(message = "fullName is required")
    @Size(max = 150)
    private String fullName;
    @Schema(description = "User email", example = "test@example.com")

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    @Size(max = 200)
    private String email;
    @Schema(description = "Password", example = "Secret123")

    @NotBlank(message = "password is required")
    @Size(min = 8, max = 100, message = "password must be 8–100 characters")
    @jakarta.validation.constraints.Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "password must contain at least one uppercase letter, one lowercase letter, and one digit"
    )
    private String password;

    @Size(max = 30)
    private String phone;

    // Getters / Setters
    public String getFullName()          { return fullName; }
    public void   setFullName(String v)  { this.fullName = v; }
    public String getEmail()             { return email; }
    public void   setEmail(String v)     { this.email = v; }
    public String getPassword()          { return password; }
    public void   setPassword(String v)  { this.password = v; }
    public String getPhone()             { return phone; }
    public void   setPhone(String v)     { this.phone = v; }
}