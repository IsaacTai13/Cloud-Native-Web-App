package com.isaactai.cloudnativeweb.image;

import com.isaactai.cloudnativeweb.image.dto.ImageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author tisaac
 */
@RestController
@RequestMapping("/v1/product/{product_id}/image")
@RequiredArgsConstructor
public class ImageController {
    private final ImageService service;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ImageResponse uploadImage(
            @PathVariable("product_id") Long productId,
            @RequestParam("file") MultipartFile file,
            Authentication auth
    ) {
        return service.uploadProdImg(auth.getName(), productId, file);
    }

    @DeleteMapping("/{image_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteImage(
            @PathVariable("product_id") Long productId,
            @PathVariable("image_id") Long imageId,
            Authentication auth
    ) {
        service.deleteForUser(auth.getName(), productId, imageId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ImageResponse> listImages(
            @PathVariable("product_id") Long productId
    ) {
        return service.listImages(productId);
    }

    @GetMapping("/{image_id}")
    @ResponseStatus(HttpStatus.OK)
    public ImageResponse getImage(
            @PathVariable("product_id") Long productId,
            @PathVariable("image_id") Long imageId
    ) {
        return service.getImageDetails(productId, imageId);
    }
}
