package com.isaactai.cloudnativeweb.product.dto;

import io.micrometer.common.lang.Nullable;
import jakarta.validation.constraints.*;

public record ProductPatchRequest(
        @Nullable
        @Size(min = 1, max = 255, message = "name length 1-255")
        String name,

        @Nullable String description,

        @Nullable
        @Pattern(regexp = "^[a-zA-Z0-9-_.]{6,64}$",
                message = "sku must be letters/digits/-_.")
        String sku,

        @Nullable
        @Size(min = 1, max = 255, message = "manufacturer length 1-255")
        String manufacturer,

        @Nullable @Min(0) @Max(100) Integer quantity
) {
        @AssertTrue(message = "At least one field must be provided")
        public boolean hasAnyField() {
                return name != null || description != null || sku != null
                        || manufacturer != null || quantity != null;
        }
}
