package com.isaactai.cloudnativeweb.health;

import org.springframework.stereotype.Service;

/**
 * @author tisaac
 */
@Service
public class HealthCheckService {
    private final HealthCheckRepository healthRepo;

    public HealthCheckService(HealthCheckRepository healthRepo) {
        this.healthRepo = healthRepo;
    }

    public HealthCheck record() {
        return healthRepo.save(new HealthCheck());
    }
}
