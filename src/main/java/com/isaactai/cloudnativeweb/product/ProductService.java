package com.isaactai.cloudnativeweb.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author tisaac
 */
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository repo;


}
