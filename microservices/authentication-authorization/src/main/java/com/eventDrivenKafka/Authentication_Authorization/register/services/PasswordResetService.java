package com.eventDrivenKafka.Authentication_Authorization.register.services;

import com.eventDrivenKafka.Authentication_Authorization.register.persistence.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class PasswordResetService {
    private final UserRepository users;
    private final PasswordResetTokenRepository tokens;
    private final PasswordEncoder encoder;
    private final JavaMailSender mailSender;
    private final SecureRandom random = new SecureRandom();

    @Value("${app.password-reset.from:}")
    private String sender;
    @Value("${app.password-reset.base-url:}")
    private String resetBaseUrl;

    public PasswordResetService(UserRepository users, PasswordResetTokenRepository tokens,
                                PasswordEncoder encoder, JavaMailSender mailSender) {
        this.users = users;
        this.tokens = tokens;
        this.encoder = encoder;
        this.mailSender = mailSender;
    }

    // Do not reveal whether an email address exists and never return or log a reset token.
    @Transactional
    public void requestReset(String email) {
        if (email == null || email.isBlank()) return;
        users.findByEmail(email.trim()).filter(user -> Boolean.TRUE.equals(user.getEmailVerified()))
                .ifPresent(user -> {
                    if (sender == null || sender.isBlank() || resetBaseUrl == null
                            || !resetBaseUrl.startsWith("https://")) {
                        throw new IllegalStateException("A verified HTTPS reset URL and SMTP sender must be configured");
                    }
                    byte[] bytes = new byte[32];
                    random.nextBytes(bytes);
                    String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
                    tokens.saveAndFlush(new PasswordResetToken(user, hash(rawToken), Instant.now().plus(15, ChronoUnit.MINUTES)));
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setFrom(sender);
                    message.setTo(user.getEmail());
                    message.setSubject("Password reset request");
                    message.setText("Use this link within 15 minutes: " + resetBaseUrl + "?token=" + rawToken
                            + "\nIf you did not request this, ignore this message.");
                    mailSender.send(message);
                });
    }

    @Transactional
    public boolean resetPassword(String rawToken, String newPassword) {
        if (rawToken == null || !rawToken.matches("[A-Za-z0-9_-]{43}")
                || newPassword == null || newPassword.length() < 12 || newPassword.length() > 128) return false;

        return tokens.findAndLockByHash(hash(rawToken))
                .filter(token -> token.getUsedAt() == null && token.getExpiresAt().isAfter(Instant.now()))
                .map(token -> {
                    token.getUser().setPassword(encoder.encode(newPassword));
                    token.getUser().setAuthToken(null);
                    users.save(token.getUser());
                    token.setUsedAt(Instant.now());
                    tokens.save(token);
                    return true;
                }).orElse(false);
    }

    private static String hash(String rawToken) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
