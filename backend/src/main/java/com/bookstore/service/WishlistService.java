package com.bookstore.service;

import com.bookstore.dto.WishlistDto;
import com.bookstore.model.Product;
import com.bookstore.model.User;
import com.bookstore.model.Wishlist;
import com.bookstore.repository.ProductRepository;
import com.bookstore.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    public WishlistService(WishlistRepository wishlistRepository,
                           ProductRepository productRepository) {
        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
    }

    public List<WishlistDto> getWishlist(Long userId) {
        return wishlistRepository.findByUserId(userId)
                .stream()
                .map(WishlistDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addToWishlist(User user, Long productId) {
        if (wishlistRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            return;
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found: " + productId));
        wishlistRepository.save(new Wishlist(user, product));
    }

    @Transactional
    public void removeFromWishlist(Long userId, Long productId) {
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
    }
}
