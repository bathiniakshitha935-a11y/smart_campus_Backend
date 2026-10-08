package com.smartcampus.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}

    public record CredentialsRequest(
            @NotBlank @Email @Size(max = 320) String email,
            @NotBlank @Size(min = 12, max = 72) String password
    ) {}

    public record AuthResponse(String token, String tokenType, long expiresInSeconds,
                               String email, Role role) {}

    public record UserResponse(String email, Role role) {}
}
