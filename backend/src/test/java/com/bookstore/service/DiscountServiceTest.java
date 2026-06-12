package com.bookstore.service;

import com.bookstore.dto.ApplyDiscountRequest;
import com.bookstore.model.Product;
import com.bookstore.model.User;
import com.bookstore.model.Wishlist;
import com.bookstore.repository.ProductRepository;
import com.bookstore.repository.WishlistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscountServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private WishlistRepository wishlistRepository;
    @Mock private EmailService emailService;

    @InjectMocks private DiscountService discountService;

    private Product product(long id, String price) {
        Product p = new Product();
        ReflectionTestUtils.setField(p, "id", id);
        p.setName("Dune");
        BigDecimal val = new BigDecimal(price);
        p.setPrice(val);
        p.setOriginalPrice(val);
        p.setDiscountRate(BigDecimal.ZERO);
        return p;
    }

    private ApplyDiscountRequest request(List<Long> ids, String rate) {
        ApplyDiscountRequest req = new ApplyDiscountRequest();
        req.setProductIds(ids);
        req.setDiscountRate(new BigDecimal(rate));
        return req;
    }

    @Test
    void applyDiscount_setsDiscountedPrice_andSnapshotsOriginal() {
        Product p = product(1L, "100.00");
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        when(wishlistRepository.findByProductIdIn(any())).thenReturn(List.of());

        discountService.applyDiscount(request(List.of(1L), "20"));

        assertEquals(0, new BigDecimal("100.00").compareTo(p.getOriginalPrice()));
        assertEquals(0, new BigDecimal("80.00").compareTo(p.getPrice()));
        assertEquals(0, new BigDecimal("20").compareTo(p.getDiscountRate()));
        verify(productRepository).save(p);
    }

    @Test
    void applyDiscount_notifiesWishlistOwners() {
        Product p = product(1L, "50.00");
        User owner = new User();
        ReflectionTestUtils.setField(owner, "id", 7L);
        owner.setName("Demo");
        owner.setEmail("demo@test.com");

        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        when(wishlistRepository.findByProductIdIn(any())).thenReturn(List.of(new Wishlist(owner, p)));

        discountService.applyDiscount(request(List.of(1L), "10"));

        verify(emailService).sendDiscountNotification(eq("demo@test.com"), eq("Demo"), anyList());
    }

    @Test
    void applyDiscount_throws_whenNoProductsSelected() {
        assertThrows(IllegalArgumentException.class,
                () -> discountService.applyDiscount(request(List.of(), "20")));
    }

    @Test
    void applyDiscount_throws_whenRateOutOfRange() {
        assertThrows(IllegalArgumentException.class,
                () -> discountService.applyDiscount(request(List.of(1L), "95")));
    }

    @Test
    void removeDiscount_restoresOriginalPrice() {
        Product p = product(1L, "100.00");
        // simulate an active 20% discount
        p.setOriginalPrice(new BigDecimal("100.00"));
        p.setPrice(new BigDecimal("80.00"));
        p.setDiscountRate(new BigDecimal("20"));
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));

        discountService.removeDiscount(1L);

        assertEquals(0, new BigDecimal("100.00").compareTo(p.getPrice()));
        assertEquals(0, BigDecimal.ZERO.compareTo(p.getDiscountRate()));
        assertNull(p.getOriginalPrice());
        verify(productRepository).save(p);
    }

    @Test
    void removeDiscount_throws_whenProductNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> discountService.removeDiscount(99L));
    }
}
