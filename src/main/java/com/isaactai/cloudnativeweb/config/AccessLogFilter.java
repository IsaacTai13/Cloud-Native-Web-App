package com.isaactai.cloudnativeweb.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @author tisaac
 */
@Component
public class AccessLogFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        long start = System.currentTimeMillis();

        try {
            chain.doFilter(req, res);
        } finally {
            long took = System.currentTimeMillis() - start;
            int status = res.getStatus();
            var log = LoggerFactory.getLogger("ACCESS");

            String method   = req.getMethod();
            String uri      = req.getRequestURI();
            String label    = String.valueOf(req.getAttribute("access.label"));
            String exName   = String.valueOf(req.getAttribute("error.exception"));

            String msgSuccess = String.valueOf(req.getAttribute("access.success"));      // success (general or override)
            String msgWarnGen = String.valueOf(req.getAttribute("access.clientWarn"));   // general warn from @AccessNote
            String msgErrGen  = String.valueOf(req.getAttribute("access.serverError"));  // general error from @AccessNote
            String msgOverride= String.valueOf(req.getAttribute("error.message"));       // detailed reason set per-request

            if (status >= 500) {
                // 5xx → ERROR with stack trace (if available)
                Throwable t = (Throwable) req.getAttribute("error.throwable");
                // combine general + detailed
                String msg = combine(msgErrGen, msgOverride);
                if (t != null) {
                    log.error("{} {} [{}] took={}ms err={} msg={}", method, uri, label, took, exName, msg, t);
                } else {
                    log.error("{} {} [{}] took={}ms err={} msg={}", method, uri, label, took, exName, msg);
                }

            } else if (status >= 400) {
                // 4xx → WARN (expected client error; no stack)
                boolean expected = Boolean.TRUE.equals(req.getAttribute("error.expected"));
                String code      = String.valueOf(req.getAttribute("error.code"));
                // combine general + detailed
                String msg = combine(msgWarnGen, msgOverride);

                if (expected && code != null && msg != null && !msg.isBlank()) {
                    log.warn("{} {} [{}] took={}ms code={} err={} msg={}", method, uri, label, took, code, exName, msg);
                } else if (msg != null && !msg.isBlank()) {
                    log.warn("{} {} [{}] took={}ms msg={}", method, uri, label, took, msg);
                } else {
                    log.warn("{} {} [{}] took={}ms", method, uri, label, took);
                }

            } else {
                // 2xx → INFO
                if (msgSuccess != null && !msgSuccess.isBlank()) {
                    log.info("{} {} [{}] took={}ms msg={}", method, uri, label, took, msgSuccess);
                } else {
                    log.info("{} {} [{}] took={}ms", method, uri, label, took);
                }
            }
        }
    }

    private static String combine(String general, String override) {
        String g = (general == null || general.isBlank()) ? null : general;
        String o = (override == null || override.isBlank()) ? null : override;
        if (g != null && o != null) return g + " - " + o;      // e.g., "Health Check failed - Query parameters are not allowed"
        return (o != null) ? o : g;                            // only one exists
    }

}
