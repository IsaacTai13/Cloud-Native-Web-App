package com.isaactai.cloudnativeweb.health;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author tisaac
 */
public interface HealthCheckRepository extends JpaRepository<HealthCheck, Long> {
}
