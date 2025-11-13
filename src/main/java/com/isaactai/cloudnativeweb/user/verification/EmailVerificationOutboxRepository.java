package com.isaactai.cloudnativeweb.user.verification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author tisaac
 */
public interface EmailVerificationOutboxRepository extends JpaRepository<EmailVerificationOutbox, Long> {
    List<EmailVerificationOutbox> findTop20ByStatusOrderByCreatedAtAsc(String status);
}
