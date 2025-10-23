package com.isaactai.cloudnativeweb.image;

import com.isaactai.cloudnativeweb.product.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * @author tisaac
 */
@Entity
@Table(name = "images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "s3_bucket_path", nullable = false, length = 1024)
    private String s3BucketPath;

    @Column(name = "date_created", nullable = false, insertable = false, updatable = false)
    private Instant dateCreated;
}
