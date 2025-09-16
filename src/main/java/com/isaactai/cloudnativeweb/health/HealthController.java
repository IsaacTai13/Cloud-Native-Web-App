package com.isaactai.cloudnativeweb.health;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author tisaac
 */
@RestController
public class HealthController {
    private final HealthCheckService service;
    private final HealthProbeService healthProbe;

    public HealthController(HealthCheckService service, HealthProbeService healthProbe) {
        this.service = service;
        this.healthProbe = healthProbe;
    }

    // For general-purpose lightweight health checks under heavy load.
    // Inserts a new record into the database each time.
    // Does not return any response body.
    @GetMapping("/healthz")
    public ResponseEntity<Void> healthz(HttpServletRequest request) {
        try {
            if (request.getInputStream().read() != -1) {
                return ResponseEntity.badRequest().build();
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        try {
            service.record();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    // Ensures this endpoint only supports GET.
    // Exists to bypass Spring Boot's default error handling
    // (which would otherwise return a JSON error body by default).
    @RequestMapping(
            value = "/healthz",
            method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH}
    )
    public ResponseEntity<Void> healthzWrongMethod() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    // For detailed health checks.
    // Returns a JSON response with service and dependency status.
    // Does not insert a new record into the database.
    @GetMapping(value = "/api/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HealthProbeService.HealthResponse> probe(HttpServletRequest request) {
        try {
            if (request.getInputStream().read() != -1) {
                return ResponseEntity.badRequest().build();
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        try {
            var result = healthProbe.probe();
            var status = result.healthy() ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
            return ResponseEntity.status(status).body(result.body());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
}
