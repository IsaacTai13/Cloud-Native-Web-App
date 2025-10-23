package com.isaactai.cloudnativeweb.image.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * @author tisaac
 */
public record ImageResponse (
    @JsonProperty("image_id")
    Long imageId,

    @JsonProperty("product_id")
    Long productId,

    @JsonProperty("file_name")
    String fileName,

    @JsonProperty("date_created")
    Instant dateCreated,

    @JsonProperty("s3_bucket_path")
    String s3BucketPath

) {}
