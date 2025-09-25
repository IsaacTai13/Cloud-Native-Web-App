package com.isaactai.cloudnativeweb.product.dto;

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
    Instant dateAdded,
    Instant dateLastUpdated,
    Long ownerUserId
) {}
