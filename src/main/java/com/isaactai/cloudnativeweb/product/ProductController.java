package com.isaactai.cloudnativeweb.product;

import com.isaactai.cloudnativeweb.product.dto.ProductCreateRequest;
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
}
