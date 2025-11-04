package com.isaactai.cloudnativeweb.config;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author tisaac
 */
@Aspect
@Component
public class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    // Pointcut: match any method inside classes annotated with @RestController
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerMethods() {}

    /**
     * Single around advice to log:
     *  - START: before controller method execution (prints HTTP method + URI + args)
     *  - SUCCESS: after normal return (prints duration)
     *  - ERROR: on exception (prints duration + exception)
     */
    @Around("controllerMethods()")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();

        // Try to get current HttpServletRequest (may be null in non-web threads)
        HttpServletRequest req = null;
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        if (ra instanceof ServletRequestAttributes sra) {
            req = sra.getRequest();
        }

        // Build safe fields
        final String httpMethod = (req != null) ? req.getMethod() : "N/A";
        final String uri = (req != null) ? req.getRequestURI() : "N/A";
        final String query = (req != null && req.getQueryString() != null) ? "?" + req.getQueryString() : "";
        final String className = pjp.getSignature().getDeclaringTypeName();
        final String methodName = pjp.getSignature().getName();

        // START
        logger.info("[START] {} {}{} -> {}.{}()",
                httpMethod, uri, query, className, methodName);

        try {
            Object result = pjp.proceed();

            // SUCCESS
            long tookMs = System.currentTimeMillis() - start;
            logger.info("[END] {} {} <- {}.{}() took={}ms",
                    httpMethod, uri, className, methodName, tookMs);

            return result;
        } catch (Throwable ex) {
            long tookMs = System.currentTimeMillis() - start;

            StackTraceElement[] st = ex.getStackTrace();
            String file = (st.length > 0) ? st[0].getFileName()   : "unknown";
            int    line = (st.length > 0) ? st[0].getLineNumber() : -1;

            logger.info("[END] {} {} !! {}.{}() at {}:{} took={}ms",
                    httpMethod, uri, className, methodName, file, line, tookMs);

            throw ex; // rethrow such a normal exception handling still applies
        }
    }
}
