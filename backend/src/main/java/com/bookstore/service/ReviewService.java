package com.bookstore.service;

import com.bookstore.dto.CommentDto;
import com.bookstore.dto.CommentRequest;
import com.bookstore.dto.RatingDto;
import com.bookstore.dto.RatingRequest;
import com.bookstore.model.Product;
import com.bookstore.model.Review;
import com.bookstore.model.User;
import com.bookstore.repository.OrderRepository;
import com.bookstore.repository.ProductRepository;
import com.bookstore.repository.ReviewRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         ProductRepository productRepository,
                         OrderRepository orderRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    public RatingDto getRating(Long productId) {
        Double avg = reviewRepository.findAverageScoreByProductId(productId);
        Long count = reviewRepository.findCountByProductId(productId);
        return new RatingDto(avg, count);
    }

    @Transactional
    public RatingDto submitRating(User user, Long productId, RatingRequest req) {
        if (req.getScore() == null || req.getScore() < 1 || req.getScore() > 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Score must be between 1 and 10");
        }
        if (!orderRepository.existsDeliveredOrderForUserAndProduct(user.getId(), productId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only rate a product after it has been delivered to you");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product " + productId + " not found"));

        Review review = reviewRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElse(new Review(user, product, req.getScore()));
        review.setScore(req.getScore());
        reviewRepository.save(review);

        return getRating(productId);
    }

    @Transactional
    public CommentDto submitComment(User user, Long productId, CommentRequest req) {
        if (req.getContent() == null || req.getContent().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment content cannot be empty");
        }
        Review review = reviewRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "You must submit a rating before adding a comment"));
        review.setContent(req.getContent().trim());
        review.setContentApproved(false);
        return new CommentDto(reviewRepository.save(review));
    }

    public List<CommentDto> getApprovedComments(Long productId) {
        return reviewRepository
                .findByProductIdAndContentIsNotNullAndContentApprovedTrue(productId)
                .stream()
                .map(CommentDto::new)
                .collect(Collectors.toList());
    }

    public List<CommentDto> getPendingComments() {
        return reviewRepository
                .findByContentIsNotNullAndContentApprovedFalse()
                .stream()
                .map(CommentDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void approveComment(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("Review " + reviewId + " not found"));
        review.setContentApproved(true);
        reviewRepository.save(review);
    }

    @Transactional
    public void rejectComment(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("Review " + reviewId + " not found"));
        review.setContent(null);
        review.setContentApproved(false);
        reviewRepository.save(review);
    }
}
