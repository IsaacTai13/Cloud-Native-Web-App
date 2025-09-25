package com.isaactai.cloudnativeweb.product;

import com.isaactai.cloudnativeweb.common.exception.ForbiddenException;
import com.isaactai.cloudnativeweb.common.exception.NotFoundException;
import com.isaactai.cloudnativeweb.product.dto.ProductCreateRequest;
import com.isaactai.cloudnativeweb.product.dto.ProductResponse;
import com.isaactai.cloudnativeweb.product.dto.ProductUpdateRequest;
import com.isaactai.cloudnativeweb.product.exception.DuplicateSkuException;
import com.isaactai.cloudnativeweb.product.dto.ProductMapper;
import com.isaactai.cloudnativeweb.user.User;
import com.isaactai.cloudnativeweb.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author tisaac
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repo;
    private final UserRepository userRepo;

    @Transactional
    public ProductResponse createForUser(ProductCreateRequest req, String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (repo.existsBySku(req.sku())) {
            throw new DuplicateSkuException();
        }

        Product p = Product.builder()
                .name(req.name())
                .description(req.description())
                .sku(req.sku())
                .manufacturer(req.manufacturer())
                .quantity(req.quantity())
                .ownerUserId(user.getId())
                .build();

        Product saved = repo.save(p);
        return ProductMapper.toResponse(saved);
    }

    @Transactional
    public void updateProduct(Long productId, String username, ProductUpdateRequest req) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Product p = repo.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (!p.getOwnerUserId().equals(user.getId())) {
            throw new ForbiddenException("You cannot update this product");
        }

        if (!p.getSku().equals(req.sku()) && repo.existsBySku(req.sku())) {
            throw new DuplicateSkuException();
        }

        p.setName(req.name());
        p.setDescription(req.description());
        p.setSku(req.sku());
        p.setManufacturer(req.manufacturer());
        p.setQuantity(req.quantity());

        repo.save(p);
    }
}
