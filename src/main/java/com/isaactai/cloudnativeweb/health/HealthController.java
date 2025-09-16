package com.isaactai.cloudnativeweb.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author tisaac
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {
    private final HealthCheckService service;

    public HealthController(HealthCheckService service) {
        this.service = service;
    }

    @GetMapping("/check")
    public String check() {
        service.record();
        return "ok";
    }
}
