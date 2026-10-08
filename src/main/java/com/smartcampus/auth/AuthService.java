package com.smartcampus.auth;

import com.smartcampus.auth.AuthDtos.AuthResponse;
import com.smartcampus.auth.AuthDtos.CredentialsRequest;
import com.smartcampus.auth.AuthDtos.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class AuthService {
    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserAccountRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse registerStudent(CredentialsRequest request) {
        String email = normalizeEmail(request.email());
        validatePasswordLength(request.password());
        if (users.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }
        UserAccount user = users.save(new UserAccount(email, passwordEncoder.encode(request.password()), Role.STUDENT));
        return authResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(CredentialsRequest request) {
        String email = normalizeEmail(request.email());
        validatePasswordLength(request.password());
        UserAccount user = users.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        return authResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser(String email) {
        UserAccount user = users.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account no longer exists"));
        return new UserResponse(user.getEmail(), user.getRole());
    }

    static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static void validatePasswordLength(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be no longer than 72 UTF-8 bytes");
        }
    }

    private AuthResponse authResponse(UserAccount user) {
        return new AuthResponse(jwtService.createToken(user), "Bearer", jwtService.getExpirationSeconds(),
                user.getEmail(), user.getRole());
    }
}
