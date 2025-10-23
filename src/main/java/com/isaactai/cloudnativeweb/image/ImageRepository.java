package com.isaactai.cloudnativeweb.image;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author tisaac
 */
public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByProduct_IdOrderByDateCreatedDesc(Long productId);
}
