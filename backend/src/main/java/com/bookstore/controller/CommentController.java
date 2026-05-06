package com.bookstore.controller;

import com.bookstore.dto.CommentDto;
import com.bookstore.dto.CommentRequest;
import com.bookstore.model.User;
import com.bookstore.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class CommentController {

    private final ReviewService reviewService;

    public CommentController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/api/products/{id}/comments")
    public ResponseEntity<List<CommentDto>> getApprovedComments(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getApprovedComments(id));
    }

    @PostMapping("/api/products/{id}/comments")
    public ResponseEntity<?> submitComment(@PathVariable Long id,
                                           @AuthenticationPrincipal User user,
                                           @RequestBody CommentRequest req) {
        if (user == null) return ResponseEntity.status(401).build();
        try {
            return ResponseEntity.ok(reviewService.submitComment(user, id, req));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatus())
                    .body(Map.of("error", Objects.requireNonNullElse(e.getReason(), "Request failed")));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/api/comments/pending")
    public ResponseEntity<List<CommentDto>> getPendingComments() {
        return ResponseEntity.ok(reviewService.getPendingComments());
    }

    @PutMapping("/api/comments/{id}/approve")
    public ResponseEntity<?> approveComment(@PathVariable Long id) {
        try {
            reviewService.approveComment(id);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/api/comments/{id}/reject")
    public ResponseEntity<?> rejectComment(@PathVariable Long id) {
        try {
            reviewService.rejectComment(id);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
