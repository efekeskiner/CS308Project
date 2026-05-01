package com.bookstore.controller;

import com.bookstore.dto.WishlistDto;
import com.bookstore.model.User;
import com.bookstore.service.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/wishlist")
@CrossOrigin(origins = "http://localhost:3000")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public ResponseEntity<List<WishlistDto>> getWishlist(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(wishlistService.getWishlist(user.getId()));
    }

    @PostMapping
    public ResponseEntity<?> addToWishlist(@AuthenticationPrincipal User user,
                                           @RequestBody Map<String, Long> body) {
        if (user == null) return ResponseEntity.status(401).build();
        Long productId = body.get("productId");
        if (productId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "productId is required"));
        }
        try {
            wishlistService.addToWishlist(user, productId);
            return ResponseEntity.ok(Map.of("message", "Added to wishlist"));
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> removeFromWishlist(@AuthenticationPrincipal User user,
                                                @PathVariable Long productId) {
        if (user == null) return ResponseEntity.status(401).build();
        wishlistService.removeFromWishlist(user.getId(), productId);
        return ResponseEntity.ok(Map.of("message", "Removed from wishlist"));
    }
}
