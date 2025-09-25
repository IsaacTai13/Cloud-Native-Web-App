package com.isaactai.cloudnativeweb.product;

import com.isaactai.cloudnativeweb.common.exception.ForbiddenException;
import com.isaactai.cloudnativeweb.common.exception.NotFoundException;
import com.isaactai.cloudnativeweb.product.dto.*;
import com.isaactai.cloudnativeweb.product.exception.DuplicateSkuException;
import com.isaactai.cloudnativeweb.user.User;
import com.isaactai.cloudnativeweb.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.LocaleResolver;

/**
 * @author tisaac
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repo;
    private final UserRepository userRepo;
    private final LocaleResolver localeResolver;

    @Transactional
    public ProductResponse createForUser(ProductCreateRequest req, String username) {
        User user = getUser(username);

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
        Product p = locateOwnedProduct(productId, username);

        ensureSkuUnique(req.sku(), p.getId(), p.getSku());

        p.setName(req.name());
        p.setDescription(req.description());
        p.setSku(req.sku());
        p.setManufacturer(req.manufacturer());
        p.setQuantity(req.quantity());

        repo.save(p);
    }

    @Transactional
    public void patchProduct(Long productId, String username, ProductPatchRequest req) {
        Product p = locateOwnedProduct(productId, username);

        if (req.name() != null)         p.setName(req.name());
        if (req.description() != null)  p.setDescription(req.description());
        if (req.manufacturer() != null) p.setManufacturer(req.manufacturer());
        if (req.quantity() != null)     p.setQuantity(req.quantity());
        if (req.sku() != null) {
            ensureSkuUnique(req.sku(), p.getId(), p.getSku());
            p.setSku(req.sku());
        }

        repo.save(p);
    }

    @Transactional
    public void deleteProduct(Long productId, String username) {
        Product p = locateOwnedProduct(productId, username);
        repo.delete(p);
    }

    @Transactional
    public ProductResponse getProduct(Long productId) {
        Product p = repo.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        return ProductMapper.toResponse(p);
    }

    public User getUser(String username) {
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public Product locateOwnedProduct(Long productId, String username) {
        User user = getUser(username);
        Product p = repo.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (!p.getOwnerUserId().equals(user.getId())) {
            throw new ForbiddenException("You cannot update this product");
        }
        return p;
    }

    private void ensureSkuUnique(String newSku, Long currentId, String oldSku) {
        if (newSku != null && !newSku.equals(oldSku) && repo.existsBySkuAndIdNot(newSku, currentId)) {
            throw new DuplicateSkuException();
        }
    }
}
