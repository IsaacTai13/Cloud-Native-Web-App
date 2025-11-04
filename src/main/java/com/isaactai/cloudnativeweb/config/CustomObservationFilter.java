package com.isaactai.cloudnativeweb.config;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

/**
 * @author tisaac
 */
@Component
public class CustomObservationFilter implements ObservationFilter {

    @Override
    public Observation.Context map(Observation.Context context) {
        // Customize the observation context if needed
        // only handle http request
        if (context.getName().equals("http.server.requests")) {
            // get request from context
            Object request = context.get(HttpServletRequest.class);

            if (request instanceof HttpServletRequest httpRequest) {
                Object handler = httpRequest.getAttribute(
                        HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE
                );

                if (handler instanceof HandlerMethod handlerMethod) {
                    ApiResourceTag annotation = handlerMethod.getBeanType()
                            .getAnnotation(ApiResourceTag.class);

                    if (annotation != null) {
                        if (!annotation.resource().isEmpty()) {
                            context.addLowCardinalityKeyValue(
                                    KeyValue.of("resource", annotation.resource())
                            );
                        }

                        if (!annotation.tag().isEmpty()) {
                            context.addLowCardinalityKeyValue(
                                    KeyValue.of("custom_tag", annotation.tag())
                            );
                        }
                    }
                }
            }
        }

        return context;
    }
}
