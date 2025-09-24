package com.isaactai.cloudnativeweb.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

/**
 * @author tisaac
 */
public record UserCreateRequest (
        @JsonProperty("first_name") String firstName,
        @JsonProperty("last_name") String lastName,
        String password,
        String username,
        @JsonProperty("account_created") Instant accountCreated,
        @JsonProperty("account_updated") Instant accountUpdated
) {}
