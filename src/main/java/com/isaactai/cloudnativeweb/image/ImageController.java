package com.isaactai.cloudnativeweb.image;

import com.isaactai.cloudnativeweb.config.ApiResourceTag;
import com.isaactai.cloudnativeweb.image.dto.ImageResponse;
import com.isaactai.cloudnativeweb.logging.AccessNote;
import io.micrometer.core.annotation.Timed;
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
@ApiResourceTag(resource = "Image")
public class ImageController {
    private final ImageService service;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @AccessNote(
            label = "Image",
            success = "Image uploaded successfully",
            clientWarn = "Image upload failed",
            serverError = "Unexpected error occurred during image upload"
    )
    public ImageResponse uploadImage(
            @PathVariable("product_id") Long productId,
            @RequestParam("file") MultipartFile file,
            Authentication auth
    ) {
        return service.uploadProdImg(auth.getName(), productId, file);
    }

    @DeleteMapping("/{image_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AccessNote(
            label = "Image",
            success = "Image deleted successfully",
            clientWarn = "Image deletion failed",
            serverError = "Unexpected error occurred during image deletion"
    )
    public void deleteImage(
            @PathVariable("product_id") Long productId,
            @PathVariable("image_id") Long imageId,
            Authentication auth
    ) {
        service.deleteForUser(auth.getName(), productId, imageId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @AccessNote(
            label = "Image",
            success = "Images listed successfully",
            clientWarn = "Image listing failed",
            serverError = "Unexpected error occurred during image listing"
    )
    public List<ImageResponse> listImages(
            @PathVariable("product_id") Long productId
    ) {
        return service.listImages(productId);
    }

    @GetMapping("/{image_id}")
    @ResponseStatus(HttpStatus.OK)
    @AccessNote(
            label = "Image",
            success = "Image retrieved successfully",
            clientWarn = "Image retrieval failed",
            serverError = "Unexpected error occurred during image retrieval"
    )
    public ImageResponse getImage(
            @PathVariable("product_id") Long productId,
            @PathVariable("image_id") Long imageId
    ) {
        return service.getImageDetails(productId, imageId);
    }
}
