package com.isaactai.cloudnativeweb.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * @author tisaac
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String pwdHash;

    @Column(name = "account_created", nullable = false, updatable = false)
    private Instant createTime;

    @Column(name = "account_updated", nullable = false)
    private Instant updatedTime;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "verification_token", columnDefinition = "uuid")
    private UUID verificationToken;

    @Column(name = "verification_token_generated_at")
    private Instant tokenGenerateAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createTime = now;
        this.updatedTime   = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedTime = Instant.now();
    }
}
