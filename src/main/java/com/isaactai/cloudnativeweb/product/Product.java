package com.isaactai.cloudnativeweb.product;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * @author tisaac
 */
@Entity
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String name;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotBlank
    @Column(nullable = false, length = 128)
    private String sku;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String manufacturer;

    @Min(0) @Max(100)
    @Column(nullable = false)
    private Integer quantity;

    // readOnly fields handled by server
    @CreationTimestamp
    @Column(name = "date_added", nullable = false, updatable = false)
    private Instant dateAdded;

    @UpdateTimestamp
    @Column(name = "date_last_updated", nullable = false)
    private Instant dateLastUpdated;

    @Column(name = "owner_user_id", nullable = false, updatable = false)
    private Long ownerUserId;
}
