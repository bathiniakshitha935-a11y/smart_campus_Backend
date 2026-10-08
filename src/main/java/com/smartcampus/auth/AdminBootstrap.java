package com.smartcampus.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Locale;

@Configuration
public class AdminBootstrap {
    @Bean
    CommandLineRunner bootstrapAdmin(UserAccountRepository users, PasswordEncoder passwordEncoder,
                                     @Value("${ADMIN_EMAIL:}") String configuredEmail,
                                     @Value("${ADMIN_PASSWORD:}") String configuredPassword) {
        return args -> {
            if ((configuredEmail == null || configuredEmail.isBlank())
                    && (configuredPassword == null || configuredPassword.isBlank())) {
                return;
            }
            if (configuredEmail == null || configuredEmail.isBlank()
                    || configuredPassword == null || configuredPassword.isBlank()) {
                throw new IllegalStateException("Configure both ADMIN_EMAIL and ADMIN_PASSWORD to bootstrap an admin");
            }
            String email = configuredEmail.trim().toLowerCase(Locale.ROOT);
            int passwordBytes = configuredPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
            if (email.length() > 320 || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                throw new IllegalStateException("ADMIN_EMAIL must be a valid email address");
            }
            if (passwordBytes < 12 || passwordBytes > 72) {
                throw new IllegalStateException("ADMIN_PASSWORD must be between 12 and 72 UTF-8 bytes");
            }
            users.findByEmail(email).ifPresentOrElse(existing -> {
                if (existing.getRole() != Role.ADMIN) {
                    throw new IllegalStateException("ADMIN_EMAIL belongs to a non-admin account");
                }
            }, () -> users.save(new UserAccount(email,
                    passwordEncoder.encode(configuredPassword), Role.ADMIN)));
        };
    }
}
