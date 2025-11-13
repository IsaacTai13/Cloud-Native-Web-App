package com.isaactai.cloudnativeweb.user.verification;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * @author tisaac
 */
@Entity
@Table(name = "email_verification_outbox")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailVerificationOutbox {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    @Column(columnDefinition = "UUID")
    private UUID token;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(nullable = false)
    private String status; // "PENDING", "SENT", ...

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

}
