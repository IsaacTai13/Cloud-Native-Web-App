package com.isaactai.cloudnativeweb.product.dto;

import com.isaactai.cloudnativeweb.product.Product;

/**
 * @author tisaac
 */
public final class ProductMapper {
    private ProductMapper() {};

    public static ProductResponse toResponse(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getSku(),
                p.getManufacturer(),
                p.getQuantity(),
                p.getDateAdded(),
                p.getDateLastUpdated(),
                p.getOwnerUserId()
        );
    }
}