package com.isaactai.cloudnativeweb.image;

import com.isaactai.cloudnativeweb.common.exception.BadRequestException;
import com.isaactai.cloudnativeweb.common.exception.NotFoundException;
import com.isaactai.cloudnativeweb.config.TimedS3;
import com.isaactai.cloudnativeweb.image.dto.ImageResponse;
import com.isaactai.cloudnativeweb.image.exception.S3UploadException;
import com.isaactai.cloudnativeweb.product.Product;
import com.isaactai.cloudnativeweb.product.ProductService;
import com.isaactai.cloudnativeweb.user.User;
import com.isaactai.cloudnativeweb.user.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * @author tisaac
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {
    private final ImageRepository repo;
    private final UserService userService;
    private final ProductService prodService;
    private final TimedS3 timedS3;

    @Value("${aws.s3.bucket}") // read bucket name from .env
    private String bucketName;

    @Transactional
    public ImageResponse uploadProdImg(String name, Long productId, MultipartFile file) {
        User user = userService.getByUsername(name);
        Product product = prodService.locateOwnedProduct(
                productId, user.getUsername(), "You can't upload image for the product");

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

        try {
            timedS3.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );
        } catch (IOException e) {
            throw new S3UploadException();
        }

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

    @Transactional
    public void deleteForUser(String username, Long productId, Long imageId) {
        User user = userService.getByUsername(username);
        Product product = prodService.locateOwnedProduct(
                productId, user.getUsername(), "You cannot delete another user's image");

        Image img = repo.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Image not found"));

        if (!img.getProduct().getId().equals(product.getId())) {
            throw new BadRequestException("Image does not belong to this product");
        }

        try {
            timedS3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(img.getS3BucketPath())
                    .build());
        } catch (S3Exception e) {
            log.warn("S3 delete failed for key {}: {}", img.getS3BucketPath(), e.awsErrorDetails().errorMessage());
        }

        repo.delete(img);
    }

    @Transactional
    public List<ImageResponse> listImages(Long productId) {
        prodService.getProduct(productId); // try to find the product first

        return repo.findByProduct_IdOrderByDateCreatedDesc(productId)
                .stream()
                .map(img -> new ImageResponse(
                        img.getImageId(),
                        img.getProduct().getId(),
                        img.getFileName(),
                        img.getDateCreated(),
                        img.getS3BucketPath()
                ))
                .toList();
    }

    @Transactional
    public ImageResponse getImageDetails(Long productId, Long imageId) {
        prodService.getProduct(productId);

        Image img = repo.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Image not found"));

        return new ImageResponse(
                img.getImageId(),
                img.getProduct().getId(),
                img.getFileName(),
                img.getDateCreated(),
                img.getS3BucketPath()
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
