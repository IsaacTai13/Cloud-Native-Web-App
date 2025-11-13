package com.isaactai.cloudnativeweb.user.verification;

import com.isaactai.cloudnativeweb.messaging.SnsPublisher;
import com.isaactai.cloudnativeweb.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * @author tisaac
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailVerificationOutboxProcessor {
    private final UserRepository userRepo;
    private final EmailVerificationOutboxRepository outboxRepo;
    private final SnsPublisher publisher;

    private static final int MAX_RETRY = 10;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processPending() {
        List<EmailVerificationOutbox> events = outboxRepo.findTop20ByStatusOrderByCreatedAtAsc("PENDING");

        for (EmailVerificationOutbox e : events) {
            if (e.getRetryCount() >= MAX_RETRY) {
                e.setStatus("FAILED");
                continue;
            }

            try {
                Instant sentAt = Instant.now();
                publisher.publishEmailVerification(e.getEmail(), e.getToken().toString());

                // mark outbox event as SENT
                e.setSentAt(sentAt);
                e.setStatus("SENT");

                // update user tokenGenerateAt
                userRepo.findByUsername(e.getEmail()).ifPresent(user -> {
                    user.setTokenGenerateAt(sentAt);
                });
            } catch (Exception ex) {
                e.setRetryCount(e.getRetryCount() + 1);

                String msg = ex.getMessage();
                if (msg != null && msg.length() > 120) {
                    msg = msg.substring(0, 120) + "...";
                }

                if (e.getRetryCount() == 1) {
                    log.warn("Initial failure to send verification email for {}: {}",
                            e.getEmail(), msg);
                } else if (e.getRetryCount() == MAX_RETRY) {
                    log.warn("Give up sending verification email for {} after {} retries",
                            e.getEmail(), MAX_RETRY);
                }
            }
        }
    }
}
