package com.isaactai.cloudnativeweb.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author tisaac
 */
public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsBySku(String sku);
    boolean existsBySkuAndIdNot(String sku, Long id);
}
