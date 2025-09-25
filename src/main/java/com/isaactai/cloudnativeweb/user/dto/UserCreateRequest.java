package com.isaactai.cloudnativeweb.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * @author tisaac
 */
public record UserCreateRequest (
        @JsonProperty("first_name")
        @NotBlank(message = "First name is required")
        @Pattern(regexp = "^[A-Za-z]+$", message = "First name must contain only letters")
        String firstName,

        @JsonProperty("last_name")
        @NotBlank(message = "Last name is required")
        @Pattern(regexp = "^[A-Za-z]+$", message = "Last name must contain only letters")
        String lastName,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String password,

        @NotBlank(message = "Username (email) is required")
        @Email(message = "Username must be a valid email address")
        @Pattern(
                regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                message = "Username must be a valid email address"
        )
        String username
) {}
