package com.isaactai.cloudnativeweb.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * @author tisaac
 */
public record ProductResponse (
    Long id,
    String name,
    String description,
    String sku,
    String manufacturer,
    Integer quantity,

    @JsonProperty("date_added")
    Instant dateAdded,

    @JsonProperty("date_last_updated")
    Instant dateLastUpdated,

    @JsonProperty("owner_user_id")
    Long ownerUserId
) {}
