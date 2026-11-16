package com.eventDrivenKafka.Authentication_Authorization.register.services;

import com.eventDrivenKafka.Authentication_Authorization.register.persistence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PasswordResetServiceTest {
    private final UserRepository users = mock(UserRepository.class);
    private final PasswordResetTokenRepository tokens = mock(PasswordResetTokenRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final JavaMailSender mail = mock(JavaMailSender.class);
    private PasswordResetService service;

    @BeforeEach
    void setUp() {
        service = new PasswordResetService(users, tokens, encoder, mail);
        ReflectionTestUtils.setField(service, "sender", "no-reply@example.test");
        ReflectionTestUtils.setField(service, "resetBaseUrl", "https://example.test/reset");
    }

    @Test
    void unknownOrUnverifiedEmailDoesNotSendToken() {
        when(users.findByEmail("unknown@example.test")).thenReturn(Optional.empty());
        User unverified = new User();
        unverified.setEmail("unverified@example.test");
        unverified.setEmailVerified(false);
        when(users.findByEmail("unverified@example.test")).thenReturn(Optional.of(unverified));
        service.requestReset("unknown@example.test");
        service.requestReset("unverified@example.test");
        verifyNoInteractions(mail);
        verifyNoInteractions(tokens);
    }

    @Test
    void issuesOpaqueTokenOnlyToVerifiedEmail() {
        User verified = new User();
        verified.setEmail("verified@example.test");
        verified.setEmailVerified(true);
        when(users.findByEmail(verified.getEmail())).thenReturn(Optional.of(verified));
        service.requestReset(verified.getEmail());
        ArgumentCaptor<SimpleMailMessage> message = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mail).send(message.capture());
        assertEquals(verified.getEmail(), message.getValue().getTo()[0]);
        assertTrue(message.getValue().getText().contains("https://example.test/reset?token="));
        verify(tokens).saveAndFlush(any(PasswordResetToken.class));
    }

    @Test
    void unauthorizedInvalidTokenCannotChangePassword() {
        assertFalse(service.resetPassword("made-up-token", "a-long-new-password"));
        assertFalse(service.resetPassword(null, "a-long-new-password"));
        assertFalse(service.resetPassword("A".repeat(43), "short"));
        verifyNoInteractions(users);
        verifyNoInteractions(tokens);
    }

    @Test
    void expiredTokenCannotChangePassword() {
        User user = new User();
        PasswordResetToken token = new PasswordResetToken(user, "hash", Instant.now().minusSeconds(60));
        when(tokens.findAndLockByHash(anyString())).thenReturn(Optional.of(token));
        assertFalse(service.resetPassword("A".repeat(43), "a-long-new-password"));
        verify(users, never()).save(any(User.class));
    }

    @Test
    void successfulTokenIsConsumedAndCannotBeReplayed() {
        User user = new User();
        user.setPassword("oldHash");
        PasswordResetToken token = new PasswordResetToken(user, "hash", Instant.now().plusSeconds(60));
        when(tokens.findAndLockByHash(anyString())).thenReturn(Optional.of(token));
        when(encoder.encode("a-long-new-password")).thenReturn("newHash");
        assertTrue(service.resetPassword("A".repeat(43), "a-long-new-password"));
        assertEquals("newHash", user.getPassword());
        assertNotNull(token.getUsedAt());
        assertFalse(service.resetPassword("A".repeat(43), "a-long-new-password"));
        verify(users, times(1)).save(user);
    }
}
