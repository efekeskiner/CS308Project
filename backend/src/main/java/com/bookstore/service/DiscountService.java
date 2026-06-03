package com.bookstore.service;

import com.bookstore.dto.ApplyDiscountRequest;
import com.bookstore.model.Product;
import com.bookstore.model.User;
import com.bookstore.model.Wishlist;
import com.bookstore.repository.ProductRepository;
import com.bookstore.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Sales-manager discount management (Req 11). Applying a discount snapshots the
 * original price, recomputes the effective price, and notifies wishlist owners.
 * Set-base-price (without a discount) lives separately in {@link ProductService}.
 */
@Service
public class DiscountService {

    private static final BigDecimal MAX_RATE = BigDecimal.valueOf(90);

    private final ProductRepository productRepository;
    private final WishlistRepository wishlistRepository;
    private final EmailService emailService;

    public DiscountService(ProductRepository productRepository,
                           WishlistRepository wishlistRepository,
                           EmailService emailService) {
        this.productRepository = productRepository;
        this.wishlistRepository = wishlistRepository;
        this.emailService = emailService;
    }

    @Transactional
    public void applyDiscount(ApplyDiscountRequest req) {
        if (req == null || req.getProductIds() == null || req.getProductIds().isEmpty()) {
            throw new IllegalArgumentException("At least one product must be selected");
        }
        BigDecimal rate = req.getDiscountRate();
        if (rate == null || rate.signum() <= 0 || rate.compareTo(MAX_RATE) > 0) {
            throw new IllegalArgumentException("Discount rate must be between 1 and 90");
        }

        BigDecimal factor = BigDecimal.ONE.subtract(rate.divide(BigDecimal.valueOf(100)));
        List<Long> changedProductIds = new ArrayList<>();

        for (Long id : req.getProductIds()) {
            Product p = productRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Product " + id + " not found"));

            // idempotency: re-applying the same rate is a no-op (no notification)
            BigDecimal current = p.getDiscountRate() != null ? p.getDiscountRate() : BigDecimal.ZERO;
            if (current.compareTo(rate) == 0) continue;

            if (p.getOriginalPrice() == null) {
                p.setOriginalPrice(p.getPrice());
            }
            p.setDiscountRate(rate);
            p.setPrice(p.getOriginalPrice().multiply(factor).setScale(2, RoundingMode.HALF_UP));
            productRepository.save(p);
            changedProductIds.add(id);
        }

        if (!changedProductIds.isEmpty()) {
            notifyWishlistOwners(changedProductIds);
        }
    }

    @Transactional
    public void removeDiscount(Long productId) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product " + productId + " not found"));
        if (p.getOriginalPrice() != null) {
            p.setPrice(p.getOriginalPrice());
        }
        p.setDiscountRate(BigDecimal.ZERO);
        p.setOriginalPrice(null);
        productRepository.save(p);
    }

    private void notifyWishlistOwners(List<Long> productIds) {
        List<Wishlist> entries = wishlistRepository.findByProductIdIn(productIds);
        // group affected products per user (group by id to avoid proxy identity issues)
        Map<Long, User> users = new LinkedHashMap<>();
        Map<Long, List<Product>> productsByUser = new LinkedHashMap<>();
        for (Wishlist w : entries) {
            Long uid = w.getUser().getId();
            users.putIfAbsent(uid, w.getUser());
            productsByUser.computeIfAbsent(uid, k -> new ArrayList<>()).add(w.getProduct());
        }
        for (Long uid : users.keySet()) {
            User u = users.get(uid);
            emailService.sendDiscountNotification(u.getEmail(), u.getName(), productsByUser.get(uid));
        }
    }
}
