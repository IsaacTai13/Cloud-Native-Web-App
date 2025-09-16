package com.isaactai.cloudnativeweb.health;

import jakarta.persistence.*;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import java.time.Instant;

/**
 * @author tisaac
 */
@Entity
@Table(name = "health_checks")
public class HealthCheck {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "check_id")
    private Long checkId;

    @Column(name = "check_datetime", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private Instant checkDatetime;

    @PrePersist
    void onCreate() {
        if (checkDatetime == null) checkDatetime = Instant.now();
    }

    public Long getCheckId() {
        return checkId;
    }

    public Instant getCheckDatetime() {
        return checkDatetime;
    }

    public void setCheckDatetime(Instant checkDatetime) {
        this.checkDatetime = checkDatetime;
    }
}
