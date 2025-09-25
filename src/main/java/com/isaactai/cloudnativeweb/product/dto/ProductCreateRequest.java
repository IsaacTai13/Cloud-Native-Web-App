package com.isaactai.cloudnativeweb.product.dto;

import jakarta.validation.constraints.*;

public record ProductCreateRequest(
        @NotBlank String name,
        @NotBlank String description,

        @NotBlank
        @Pattern(regexp = "^[a-zA-Z0-9]{6,18}$", message = "SKU must be 6-12 letters or digits")
        String sku,

        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9 .,&-]{2,100}$", message = "Manufacturer must be 2-100 chars, valid symbols only")
        String manufacturer,

        @NotNull @Min(0) @Max(100) Integer quantity
) {}
