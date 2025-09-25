package com.isaactai.cloudnativeweb.product.dto;

import jakarta.validation.constraints.*;

public record ProductCreateRequest(
        @NotBlank
        @Size(min = 1, max = 255, message = "name length 1-255")
        String name,

        @NotBlank String description,

        @NotBlank
        @Pattern(regexp = "^[a-zA-Z0-9-_.]{6,64}$",
                message = "sku must be letters/digits/-_.")
        String sku,

        @NotBlank
        @Size(min = 1, max = 255, message = "manufacturer length 1-255")
        String manufacturer,

        @NotNull @Min(0) @Max(100) Integer quantity
) {}
