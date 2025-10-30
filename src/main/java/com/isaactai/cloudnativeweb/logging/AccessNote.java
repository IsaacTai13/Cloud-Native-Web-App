package com.isaactai.cloudnativeweb.logging;

import java.lang.annotation.*;

/**
 * @author tisaac
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AccessNote {
    String label() default "";          // e.g., ex. ("User", "Product", "Image", etc)
    String success() default "";        // e.g., "User created"
    String clientWarn() default "";     // e.g., "User action failed" (general)
    String serverError() default "";    // e.g., "User service failed"
}
