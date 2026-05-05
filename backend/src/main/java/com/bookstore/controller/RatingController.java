package com.bookstore.controller;

import com.bookstore.dto.RatingDto;
import com.bookstore.dto.RatingRequest;
import com.bookstore.model.User;
import com.bookstore.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:3000")
public class RatingController {

    private final ReviewService reviewService;

    public RatingController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/{id}/ratings")
    public ResponseEntity<RatingDto> getRating(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getRating(id));
    }

    @PostMapping("/{id}/ratings")
    public ResponseEntity<?> submitRating(@PathVariable Long id,
                                          @AuthenticationPrincipal User user,
                                          @RequestBody RatingRequest req) {
        if (user == null) return ResponseEntity.status(401).build();
        try {
            return ResponseEntity.ok(reviewService.submitRating(user, id, req));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatus()).body(Map.of("error", e.getReason()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
