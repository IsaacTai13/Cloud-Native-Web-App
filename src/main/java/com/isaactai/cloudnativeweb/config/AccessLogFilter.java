package com.isaactai.cloudnativeweb.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @author tisaac
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AccessLogFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        long start = System.currentTimeMillis();

        try {
            chain.doFilter(req, res);
        } finally {
            long took   = System.currentTimeMillis() - start;
            int status  = res.getStatus();
            var log     = LoggerFactory.getLogger("ACCESS");

            String method = req.getMethod();
            String uri    = req.getRequestURI();

            // --- SAFE getters (no "null" literal) ---
            String labelRaw    = attr(req, "access.label");
            String exName      = attr(req, "error.exception");

            String msgSuccess  = attr(req, "access.success");      // 2xx general
            String msgWarnGen  = attr(req, "access.clientWarn");   // 4xx general
            String msgErrGen   = attr(req, "access.serverError");  // 5xx general
            String msgOverride = attr(req, "error.message");       // per-request detail
            String code        = attr(req, "error.code");

            // --- fallbacks for missing attributes ---
            String labelFinal = firstNonBlank(labelRaw, "Security");

            if (status == 401) {
                code        = firstNonBlank(code, "UNAUTHORIZED");
                msgOverride = firstNonBlank(msgOverride, "Missing or invalid credentials");
            } else if (status == 403) {
                code        = firstNonBlank(code, "FORBIDDEN");
                msgOverride = firstNonBlank(msgOverride, "Insufficient permissions");
            }

            if (status >= 500) {
                String msg = combine(msgErrGen, msgOverride);
                Throwable t = (Throwable) req.getAttribute("error.throwable");

                // shortened the msg to prevent too long
                String inlineMsgShort = abbrev(msg, 220);

                if (t != null) {
                    log.error("{} {} [{}] took={}ms{}{}",
                            method, uri, labelFinal, took,
                            nonBlank(" err=", exName),
                            nonBlank(" msg=", inlineMsgShort),
                            t);
                } else {
                    log.error("{} {} [{}] took={}ms{}{}",
                            method, uri, labelFinal, took,
                            nonBlank(" err=", exName),
                            nonBlank(" msg=", msg));
                }

            } else if (status >= 400) {
                boolean expected = Boolean.TRUE.equals(req.getAttribute("error.expected"));
                String msg = combine(msgWarnGen, msgOverride);

                if (expected && isNotBlank(code) && isNotBlank(msg)) {
                    log.warn("{} {} [{}] took={}ms code={}{}",
                            method, uri, labelFinal, took, code,
                            nonBlank(" msg=", msg));
                } else if (isNotBlank(msg)) {
                    log.warn("{} {} [{}] took={}ms msg={}", method, uri, labelFinal, took, msg);
                } else {
                    log.warn("{} {} [{}] took={}ms", method, uri, labelFinal, took);
                }

            } else {
                if (isNotBlank(msgSuccess)) {
                    log.info("{} {} [{}] took={}ms msg={}", method, uri, labelFinal, took, msgSuccess);
                } else {
                    log.info("{} {} [{}] took={}ms", method, uri, labelFinal, took);
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

    // Safely read a request attribute as String; treat missing/"null"/blank as null
    private static String attr(HttpServletRequest req, String name) {
        Object o = req.getAttribute(name);
        if (o == null) return null;
        String s = (o instanceof String) ? (String) o : String.valueOf(o);
        return (s == null || s.isBlank() || "null".equalsIgnoreCase(s)) ? null : s;
    }

    private static boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }

    private static String nonBlank(String prefix, String val) {
        return isNotBlank(val) ? (prefix + val) : "";
    }

    private static String firstNonBlank(String... ss) {
        if (ss == null) return null;
        for (String s : ss) if (isNotBlank(s)) return s;
        return null;
    }

    private static String abbrev(String s, int max) {
        if (s == null) return null;
        s = s.trim();
        if (s.length() <= max) return s;
        return s.substring(0, Math.max(0, max - 1)) + "…";
    }
}
