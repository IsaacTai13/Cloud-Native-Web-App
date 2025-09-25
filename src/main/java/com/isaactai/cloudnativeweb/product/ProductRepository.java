package com.isaactai.cloudnativeweb.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author tisaac
 */
public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsBySku(String sku);
    Optional<Product> findBySku(String sku);
}
