package com.swer313.projectstep1.auth;

import com.swer313.projectstep1.user.UserResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * الـ response بعد login أو register:
 * يرجع الـ JWT token + بيانات المستخدم.
 */
@Schema(description = "Authentication response")

public class AuthResponse {
    @Schema(description = "JWT token", example = "eyJhbGciOiJIUzI1NiJ9...")

    private String          token;
    @Schema(description = "Token type", example = "Bearer")

    private String          type = "Bearer";
    @Schema(description = "Authenticated user data")

    private UserResponseDTO user;

    public AuthResponse(String token, UserResponseDTO user) {
        this.token = token;
        this.user  = user;
    }

    // Getters
    public String          getToken() { return token; }
    public String          getType()  { return type; }
    public UserResponseDTO getUser()  { return user; }
}