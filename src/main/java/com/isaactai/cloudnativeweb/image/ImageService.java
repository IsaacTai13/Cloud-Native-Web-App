package com.isaactai.cloudnativeweb.image;

import com.isaactai.cloudnativeweb.common.exception.BadRequestException;
import com.isaactai.cloudnativeweb.image.dto.ImageResponse;
import com.isaactai.cloudnativeweb.product.Product;
import com.isaactai.cloudnativeweb.product.ProductService;
import com.isaactai.cloudnativeweb.user.User;
import com.isaactai.cloudnativeweb.user.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

/**
 * @author tisaac
 */
@Service
@RequiredArgsConstructor
public class ImageService {
    private final ImageRepository repo;
    private final UserService userService;
    private final ProductService prodService;

    @Transactional
    public ImageResponse uploadProdImg(String name, Long productId, MultipartFile file) {
        User user = userService.getByUsername(name);
        Product product = prodService.locateOwnedProduct(productId, user.getUsername());

        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Invalid file type, must be an image");
        }

        String originalName = file.getOriginalFilename();
        String safeName = sanitizeFileName(originalName);
        String key = String.format("users/%d/products/%d/%s-%s",
                user.getId(), productId, UUID.randomUUID(), safeName);

        /*
        try {
            s3Client.putObject(
                PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .build(),
                RequestBody.fromBytes(file.getBytes())
            );
        } catch (IOException e) {
            throw new RuntimeException("S3 upload failed", e);
        }
        */

        Image img = Image.builder()
                .product(product)
                .fileName(safeName)
                .s3BucketPath(key)
                .dateCreated(Instant.now())
                .build();

        Image saved = repo.save(img);

        return new ImageResponse(
                saved.getImageId(),
                saved.getProduct().getId(),
                saved.getFileName(),
                saved.getDateCreated(),
                saved.getS3BucketPath()
        );
    }

    private String sanitizeFileName(String name) {
        if (name == null || name.isBlank()) {
            return "unnamed";
        }
        String clean = name.toLowerCase()
                .replaceAll("[^a-z0-9._-]", "-")
                .replaceAll("-{2,}", "-");
        return clean.length() > 100 ? clean.substring(0, 100) : clean;
    }
}
