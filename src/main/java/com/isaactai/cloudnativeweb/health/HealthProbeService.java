package com.isaactai.cloudnativeweb.health;

import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tisaac
 */
@Service
public class HealthProbeService {
    private final DataSource dataSource;

    public HealthProbeService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Perform a database health probe:
    // Builds a structured health response including DB status, latency, and metadata.
    public HealthResult probe() {
        Instant start = Instant.now();

        boolean dbUp = false;
        String error = null;
        String product = null;
        String version = null;
        Integer ping = null;

        try (Connection conn = dataSource.getConnection()) {
            dbUp = conn.isValid(2);
            var md = conn.getMetaData();
            product = md.getDatabaseProductName();
            version = md.getDatabaseProductVersion();

            try (var stmt = conn.createStatement()) {
                try (var rs = stmt.executeQuery("select 1")) {
                    if (rs.next()) ping = rs.getInt(1);
                }
            }
        } catch (Exception e) {
            error = "Database unreachable";
            dbUp = false;
        }

        Instant end = Instant.now();
        long latencyMs = Duration.between(start, end).toMillis();

        // dependencies
        Map<String, Object> db = new HashMap<>();
        db.put("status", dbUp ? "UP" : "DOWN");
        boolean reachable = dbUp && (ping != null && ping == 1);
        db.put("reachable", reachable);
        db.put("latencyMs", latencyMs);
        if (product != null) db.put("product", product);
        if (version != null) db.put("version", version);
        if (error != null) db.put("error", error);

        Map<String, Object> dependencies = Map.of("database", db);

        HealthResponse body = new HealthResponse(
                "Cloud-Native-Web",
                dbUp ? "UP" : "DOWN",
                end.toString(),
                dependencies
        );

        return new HealthResult(dbUp, body);
    }

    public record HealthResponse(
            String service,
            String status,
            String time,
            Map<String, Object> dependencies
    ) {}

    public record HealthResult(boolean healthy, HealthResponse body) {}
}
