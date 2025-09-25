package com.isaactai.cloudnativeweb.product;

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
    public ProductResponse create(
            @Valid @RequestBody ProductCreateRequest req,
            Authentication auth
    ) {
        return service.createForUser(req, auth.getName());
    }

    @PutMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductUpdateRequest req,
            Authentication auth
    ) {
        service.updateProduct(productId, auth.getName(), req);
    }

    @PatchMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void patchProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductPatchRequest req,
            Authentication auth
    ) {
        service.patchProduct(productId, auth.getName(), req);
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(
            @PathVariable Long productId,
            Authentication auth
    ) {
        service.deleteProduct(productId, auth.getName());
    }

    @GetMapping("/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponse getProduct(@PathVariable Long productId) {
        return service.getProduct(productId);
    }
}
