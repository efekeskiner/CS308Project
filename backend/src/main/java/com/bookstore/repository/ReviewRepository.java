package com.bookstore.repository;

import com.bookstore.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByUserIdAndProductId(Long userId, Long productId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    @Query("SELECT AVG(r.score) FROM Review r WHERE r.product.id = :productId")
    Double findAverageScoreByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId")
    Long findCountByProductId(@Param("productId") Long productId);

    List<Review> findByProductId(Long productId);

    List<Review> findByProductIdAndContentIsNotNullAndContentApprovedTrue(Long productId);

    List<Review> findByContentIsNotNullAndContentApprovedFalse();
}
