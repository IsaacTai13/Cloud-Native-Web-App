package com.isaactai.cloudnativeweb.config;

import com.isaactai.cloudnativeweb.metrics.ApiObserved;
import com.isaactai.cloudnativeweb.metrics.ApiResourceTag;
import com.isaactai.cloudnativeweb.metrics.S3Observed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tisaac
 */
@Aspect
@Component
@RequiredArgsConstructor
public class MetricsAspect {
    private final MeterRegistry registry;

    @Around("@annotation(apiObs)")
    public Object apiAround(ProceedingJoinPoint pjp, ApiObserved apiObs) throws Throwable {
        long start = System.nanoTime();
        boolean success = true;
        try {
            return pjp.proceed(); // Proceed with the original method call (Controller handler)
        } catch (Throwable t) {
            success = false;
            throw t;
        } finally {
            long ns = System.nanoTime() - start;
            double ms = ns / 1_000_000.0;

            // Extract HTTP method and URI pattern from the current request
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            String method = (attrs != null && attrs.getRequest() != null)
                    ? attrs.getRequest().getMethod()
                    : "UNKNOWN";

            String uriPattern = (attrs != null && attrs.getRequest() != null)
                    ? String.valueOf(attrs.getRequest().getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE))
                    : "UNKNOWN";

            // read @ApiResourceTag
            MethodSignature sig = (MethodSignature) pjp.getSignature();
            Class<?> cls = sig.getDeclaringType();
            ApiResourceTag tag = cls.getAnnotation(ApiResourceTag.class);
            if (tag == null) tag = sig.getMethod().getAnnotation(ApiResourceTag.class);

            List<Tag> tags = new ArrayList<>();
            tags.add(Tag.of("method", method));
            tags.add(Tag.of("uri", uriPattern));
            tags.add(Tag.of("outcome", success ? "success" : "error"));
            if (tag != null) {
                if (!tag.resource().isBlank()) tags.add(Tag.of("resource", tag.resource()));
                if (!tag.tag().isBlank())      tags.add(Tag.of("custom_tag", tag.tag()));
            }

            Counter.builder(apiObs.name() + ".count")
                    .tags(tags)
                    .register(registry)
                    .increment();

            // Latency: use DistributionSummary to record the value directly in milliseconds
            // (Timer reports in seconds and uploads as sum/count/max; using Summary makes the CloudWatch chart show ms directly)
            DistributionSummary.builder(apiObs.name() + ".time.ms")
                    .tags(tags)
                    .register(registry)
                    .record(ms);
        }
    }

    @Around("execution(* org.springframework.data.repository.Repository+.*(..)) " +
            "|| within(@org.springframework.stereotype.Repository *)")
    public Object dbAround(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.nanoTime();
        boolean success = true;
        try {
            return pjp.proceed();
        } catch (Throwable t) {
            success = false;
            throw t;
        }finally {
            double ms = (System.nanoTime() - start) / 1_000_000.0;
            String op = pjp.getSignature().getName();
            DistributionSummary.builder("app.db.query.time.ms")
                    .baseUnit("milliseconds")
                    .tag("operation", op)
                    .tag("success", String.valueOf(success))
                    .register(registry)
                    .record(ms);
        }
    }

    @Around("@annotation(s3Obs)")
    public Object s3Around(ProceedingJoinPoint pjp, S3Observed s3Obs) throws Throwable {
        long start = System.nanoTime();
        boolean success = true;
        try {
            return pjp.proceed();
        } catch (Throwable t) {
            success = false;
            throw t;
        } finally {
            double ms = (System.nanoTime() - start) / 1_000_000.0;
            DistributionSummary.builder(s3Obs.name() + ".time.ms")
                    .baseUnit("milliseconds")
                    .tag("success", String.valueOf(success))
                    .register(registry)
                    .record(ms);
        }
    }
}
