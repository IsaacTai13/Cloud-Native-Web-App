package com.isaactai.cloudnativeweb.user.verification;

import com.isaactai.cloudnativeweb.common.exception.BadRequestException;
import com.isaactai.cloudnativeweb.common.exception.ForbiddenException;
import com.isaactai.cloudnativeweb.common.exception.NotFoundException;
import com.isaactai.cloudnativeweb.user.User;
import com.isaactai.cloudnativeweb.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * @author tisaac
 */
@Service
@AllArgsConstructor
public class EmailVerificationService {
    private final UserRepository userRepo;
    private final EmailVerificationOutboxRepository outboxRepo;

    @Transactional
    public void enqueueVerificationEmail(User user) {

        UUID token = UUID.randomUUID();
        user.setVerificationToken(token);
        user.setTokenGenerateAt(null); // sentAt will be set when the email is actually sent

        // 2. write to outbox（same transaction）
        EmailVerificationOutbox event = new EmailVerificationOutbox();
        event.setEmail(user.getUsername());
        event.setToken(token);
        event.setCreatedAt(Instant.now());
        event.setStatus("PENDING");
        outboxRepo.save(event);
    }

    @Transactional
    public void verify(String username, String tokenStr, Instant now) {
        UUID token = parseUuidOr400(tokenStr);
        User u = getByUsername(username);

        UUID userToken = u.getVerificationToken();
        if (userToken == null || !userToken.equals(token)) {
            throw new ForbiddenException("Token mismatch");
        }

        // check if expired
        Instant sentAt = u.getTokenGenerateAt();
        if (sentAt == null) {
            throw new ForbiddenException("Token not sent yet");
        }

        if (now.isAfter(sentAt.plusSeconds(60))) {
            throw new ForbiddenException("Token expired");
        }

        u.setEmailVerified(true);
        u.setVerificationToken(null);
        u.setTokenGenerateAt(null);
    }

    private static UUID parseUuidOr400(String s) {
        if (s == null) throw new BadRequestException("Token cannot be null");
        try {
            return UUID.fromString(s); // take uuid string and convert to the actual UUID object
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid token format");
        }
    }

    protected User getByUsername(String username) {
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
