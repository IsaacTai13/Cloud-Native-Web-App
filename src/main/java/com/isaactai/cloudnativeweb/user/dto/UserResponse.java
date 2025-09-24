package com.isaactai.cloudnativeweb.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record UserResponse(
        Long id,
        @JsonProperty("first_name") String firstName,
        @JsonProperty("last_name") String lastName,
        String username,
        @JsonProperty("account_created") Instant accountCreated,
        @JsonProperty("account_updated") Instant accountUpdated
) {}
