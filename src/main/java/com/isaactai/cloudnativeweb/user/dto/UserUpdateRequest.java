package com.isaactai.cloudnativeweb.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * @author tisaac
 */
@JsonIgnoreProperties(ignoreUnknown = false)
public record UserUpdateRequest (
    @JsonProperty("first_name")
    @Pattern(regexp = "^[A-Za-z]+$", message = "First name must contain only letters")
    String firstName,

    @JsonProperty("last_name")
    @Pattern(regexp = "^[A-Za-z]+$", message = "Last name must contain only letters")
    String lastName,

    @JsonProperty("password")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    String password


) {}
