package com.isaactai.cloudnativeweb.product;

import com.isaactai.cloudnativeweb.logging.AccessNote;
import com.isaactai.cloudnativeweb.product.dto.ProductCreateRequest;
import com.isaactai.cloudnativeweb.product.dto.ProductPatchRequest;
import com.isaactai.cloudnativeweb.product.dto.ProductResponse;
import com.isaactai.cloudnativeweb.product.dto.ProductUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * @author tisaac
 */
@RestController
@RequestMapping("/v1/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @AccessNote(
            label = "Product",
            success = "Product created successfully",
            clientWarn = "Product creation failed",
            serverError = "Unexpected error occurred during product creation"
    )
    public ProductResponse create(
            @Valid @RequestBody ProductCreateRequest req,
            Authentication auth
    ) {
        return service.createForUser(req, auth.getName());
    }

    @PutMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AccessNote(
            label = "Product",
            success = "Product updated successfully",
            clientWarn = "Product update failed",
            serverError = "Unexpected error occurred during product update"
    )
    public void updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductUpdateRequest req,
            Authentication auth
    ) {
        service.updateProduct(productId, auth.getName(), req);
    }

    @PatchMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AccessNote(
            label = "Product",
            success = "Product patched successfully",
            clientWarn = "Product patch failed",
            serverError = "Unexpected error occurred during product patch"
    )
    public void patchProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductPatchRequest req,
            Authentication auth
    ) {
        service.patchProduct(productId, auth.getName(), req);
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AccessNote(
            label = "Product",
            success = "Product deleted successfully",
            clientWarn = "Product deletion failed",
            serverError = "Unexpected error occurred during product deletion"
    )
    public void deleteProduct(
            @PathVariable Long productId,
            Authentication auth
    ) {
        service.deleteProduct(productId, auth.getName());
    }

    @GetMapping("/{productId}")
    @ResponseStatus(HttpStatus.OK)
    @AccessNote(
            label = "Product",
            success = "Product retrieved successfully",
            clientWarn = "Product retrieval failed",
            serverError = "Unexpected error occurred during product retrieval"
    )
    public ProductResponse getProduct(@PathVariable Long productId) {
        return service.getProduct(productId);
    }
}
