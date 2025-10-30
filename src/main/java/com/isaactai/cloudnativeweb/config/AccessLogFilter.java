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
            String method = req.getMethod();
            String uri    = req.getRequestURI();
            String exName = String.valueOf(req.getAttribute("error.exception"));
            String msg    = String.valueOf(req.getAttribute("error.message"));

            if (status >= 500) {
                Throwable t = (Throwable) req.getAttribute("error.throwable");

                if (t != null) {
                    log.error("{} {} took={}ms err={} msg={}", method, uri, took, exName, msg, t); // full stack info
                } else {
                    log.error("{} {} took={}ms", method, uri, took);
                }
            } else if (status >= 400) {
                boolean expected = Boolean.TRUE.equals(req.getAttribute("error.expected"));
                String code = String.valueOf(req.getAttribute("error.code"));

                if (expected && code != null && msg != null) {
                    log.warn("{} {} took={}ms code={} err={} msg={}", method, uri, took, code, exName, msg); // no stack
                } else {
                    log.warn("{} {} took={}ms", method, uri, took);
                }
            } else {
                log.info("{} {} took={}ms", method, uri, took);
            }
        }
    }
}
