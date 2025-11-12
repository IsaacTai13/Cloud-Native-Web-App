package com.isaactai.cloudnativeweb.user.verification;

import com.isaactai.cloudnativeweb.common.exception.BadRequestException;
import com.isaactai.cloudnativeweb.common.exception.ForbiddenException;
import com.isaactai.cloudnativeweb.common.exception.NotFoundException;
import com.isaactai.cloudnativeweb.user.User;
import com.isaactai.cloudnativeweb.user.UserRepository;
import com.isaactai.cloudnativeweb.user.UserService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * @author tisaac
 */
@Service
@AllArgsConstructor
public class EmailVerificationService {
    private final UserRepository userRepo;
    private static final Duration TOKEN_TTL = Duration.ofMinutes(1);

    @Transactional
    public UUID issueToken(String username, Instant now) {
        User u = getByUsername(username);
        UUID token = UUID.randomUUID();
        u.setEmailVerified(false);
        u.setVerificationToken(token);
        u.setTokenGenerateAt(now);
        return token;
    }

    @Transactional
    public void verify(String username, String tokenStr, Instant now) {
        UUID token = parseUuidOr400(tokenStr);
        User u = getByUsername(username);

        UUID userToken = u.getVerificationToken();
        if (u.getVerificationToken() == null || !u.getVerificationToken().equals(token)) {
            throw new ForbiddenException("Token mismatch");
        }

        // check if expired
        Instant tokenGenerate = u.getTokenGenerateAt();
        if (tokenGenerate == null || now.isAfter(tokenGenerate.plusSeconds(60))) {
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

    private User getByUsername(String username) {
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
