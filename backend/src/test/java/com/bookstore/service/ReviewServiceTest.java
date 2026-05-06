package com.bookstore.service;

import com.bookstore.dto.CommentRequest;
import com.bookstore.dto.RatingRequest;
import com.bookstore.model.*;
import com.bookstore.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

      @Mock private ReviewRepository reviewRepository;
      @Mock private ProductRepository productRepository;
      @Mock private OrderRepository orderRepository;

      @InjectMocks
      private ReviewService reviewService;

      @Test
      void submitRating_throwsWhenScoreIsNull() {
                User user = new User();
                RatingRequest req = new RatingRequest();
                req.setScore(null);
                assertThrows(ResponseStatusException.class,
                                             () -> reviewService.submitRating(user, 1L, req));
            }

      @Test
      void submitRating_throwsWhenScoreBelowOne() {
                User user = new User();
                RatingRequest req = new RatingRequest();
                req.setScore(0);
                assertThrows(ResponseStatusException.class,
                                             () -> reviewService.submitRating(user, 1L, req));
            }

      @Test
      void submitRating_throwsWhenScoreAboveTen() {
                User user = new User();
                RatingRequest req = new RatingRequest();
                req.setScore(11);
                assertThrows(ResponseStatusException.class,
                                             () -> reviewService.submitRating(user, 1L, req));
            }

      @Test
      void submitRating_throwsWhenUserHasNotPurchasedProduct() {
                User user = new User();
                ReflectionTestUtils.setField(user, "id", 1L);
                RatingRequest req = new RatingRequest();
                req.setScore(5);

                when(orderRepository.existsDeliveredOrderForUserAndProduct(1L, 10L))
                        .thenReturn(false);

                assertThrows(ResponseStatusException.class,
                                             () -> reviewService.submitRating(user, 10L, req));
            }

      @Test
      void submitRating_throwsWhenProductNotFound() {
                User user = new User();
                ReflectionTestUtils.setField(user, "id", 1L);
                RatingRequest req = new RatingRequest();
                req.setScore(7);

                when(orderRepository.existsDeliveredOrderForUserAndProduct(1L, 10L))
                        .thenReturn(true);
                when(productRepository.findById(10L)).thenReturn(Optional.empty());

                assertThrows(NoSuchElementException.class,
                                             () -> reviewService.submitRating(user, 10L, req));
            }

      @Test
      void submitRating_createsNewReviewWhenNoneExists() {
                User user = new User();
                ReflectionTestUtils.setField(user, "id", 1L);

                Product product = new Product();
                ReflectionTestUtils.setField(product, "id", 10L);

                RatingRequest req = new RatingRequest();
                req.setScore(8);

                when(orderRepository.existsDeliveredOrderForUserAndProduct(1L, 10L))
                        .thenReturn(true);
                when(productRepository.findById(10L)).thenReturn(Optional.of(product));
                when(reviewRepository.findByUserIdAndProductId(1L, 10L))
                        .thenReturn(Optional.empty());

                Review saved = new Review(user, product, 8);
                when(reviewRepository.save(any())).thenReturn(saved);
                when(reviewRepository.findAverageScoreByProductId(10L)).thenReturn(8.0);
                when(reviewRepository.findCountByProductId(10L)).thenReturn(1L);

                var dto = reviewService.submitRating(user, 10L, req);
                assertNotNull(dto);
                assertEquals(8.0, dto.getAverageRating());
            }

      @Test
      void submitComment_throwsWhenContentIsBlank() {
                User user = new User();
                CommentRequest req = new CommentRequest();
                req.setContent("   ");
                assertThrows(ResponseStatusException.class,
                                             () -> reviewService.submitComment(user, 1L, req));
            }

      @Test
      void submitComment_throwsWhenNoRatingExists() {
                User user = new User();
                ReflectionTestUtils.setField(user, "id", 1L);
                CommentRequest req = new CommentRequest();
                req.setContent("Great book!");

                when(reviewRepository.findByUserIdAndProductId(1L, 5L))
                        .thenReturn(Optional.empty());

                assertThrows(ResponseStatusException.class,
                                             () -> reviewService.submitComment(user, 5L, req));
            }

      @Test
      void approveComment_throwsWhenReviewNotFound() {
                when(reviewRepository.findById(999L)).thenReturn(Optional.empty());
                assertThrows(NoSuchElementException.class,
                                             () -> reviewService.approveComment(999L));
            }

      @Test
      void approveComment_setsContentApprovedTrue() {
                Review review = new Review();
                review.setContentApproved(false);
                when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
                when(reviewRepository.save(review)).thenReturn(review);

                reviewService.approveComment(1L);

                assertTrue(review.getContentApproved());
                verify(reviewRepository).save(review);
            }

      @Test
      void rejectComment_throwsWhenReviewNotFound() {
                when(reviewRepository.findById(999L)).thenReturn(Optional.empty());
                assertThrows(NoSuchElementException.class,
                                             () -> reviewService.rejectComment(999L));
            }

      @Test
      void rejectComment_clearsContentAndSetsApprovedFalse() {
                Review review = new Review();
                review.setContent("Some comment");
                review.setContentApproved(true);
                when(reviewRepository.findById(2L)).thenReturn(Optional.of(review));
                when(reviewRepository.save(review)).thenReturn(review);

                reviewService.rejectComment(2L);

                assertNull(review.getContent());
                assertFalse(review.getContentApproved());
                verify(reviewRepository).save(review);
            }

      @Test
      void getApprovedComments_returnsEmptyListWhenNoneExist() {
                when(reviewRepository
                                     .findByProductIdAndContentIsNotNullAndContentApprovedTrue(1L))
                        .thenReturn(List.of());

                var result = reviewService.getApprovedComments(1L);
                assertNotNull(result);
                assertTrue(result.isEmpty());
            }

      @Test
      void getPendingComments_returnsOnlyUnapproved() {
                User user = new User();
                ReflectionTestUtils.setField(user, "id", 1L);
                Product product = new Product();
                ReflectionTestUtils.setField(product, "id", 10L);
                product.setName("Some Book");

                Review r = new Review(user, product, 4);
                r.setContent("Pending comment");
                r.setContentApproved(false);
                when(reviewRepository.findByContentIsNotNullAndContentApprovedFalse())
                        .thenReturn(List.of(r));

                var result = reviewService.getPendingComments();
                assertEquals(1, result.size());
            }
  }
