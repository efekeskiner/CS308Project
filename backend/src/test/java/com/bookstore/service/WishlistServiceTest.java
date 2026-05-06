package com.bookstore.service;

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

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
  class WishlistServiceTest {

    @Mock
        private WishlistRepository wishlistRepository;

    @Mock
        private ProductRepository productRepository;

    @InjectMocks
        private WishlistService wishlistService;

    @Test
        void getWishlist_returnsEmptyListWhenUserHasNoItems() {
                  when(wishlistRepository.findByUserId(1L)).thenReturn(List.of());
                  var result = wishlistService.getWishlist(1L);
                  assertNotNull(result);
                  assertTrue(result.isEmpty());
        }

    @Test
        void getWishlist_returnsMappedDtos() {
                  User user = new User();
                  user.setId(1L);
                  Product product = new Product();
                  product.setId(10L);
                  product.setName("Clean Code");
                  Wishlist entry = new Wishlist(user, product);
                  when(wishlistRepository.findByUserId(1L)).thenReturn(List.of(entry));
                  var result = wishlistService.getWishlist(1L);
                  assertEquals(1, result.size());
        }

    @Test
        void addToWishlist_doesNothingWhenAlreadyInWishlist() {
                  User user = new User();
                  user.setId(1L);
                  when(wishlistRepository.existsByUserIdAndProductId(1L, 5L)).thenReturn(true);
                  wishlistService.addToWishlist(user, 5L);
                  verify(wishlistRepository, never()).save(any());
        }

    @Test
        void addToWishlist_throwsWhenProductNotFound() {
                  User user = new User();
                  user.setId(1L);
                  when(wishlistRepository.existsByUserIdAndProductId(1L, 99L)).thenReturn(false);
                  when(productRepository.findById(99L)).thenReturn(Optional.empty());
                  assertThrows(NoSuchElementException.class,
                                               () -> wishlistService.addToWishlist(user, 99L));
        }

    @Test
        void addToWishlist_savesNewEntryWhenProductExists() {
                  User user = new User();
                  user.setId(1L);
                  Product product = new Product();
                  product.setId(5L);
                  when(wishlistRepository.existsByUserIdAndProductId(1L, 5L)).thenReturn(false);
                  when(productRepository.findById(5L)).thenReturn(Optional.of(product));
                  wishlistService.addToWishlist(user, 5L);
                  verify(wishlistRepository).save(any(Wishlist.class));
        }

    @Test
        void removeFromWishlist_deletesEntryByUserAndProduct() {
                  wishlistService.removeFromWishlist(1L, 5L);
                  verify(wishlistRepository).deleteByUserIdAndProductId(1L, 5L);
        }

    @Test
        void removeFromWishlist_doesNotThrowWhenEntryDoesNotExist() {
                  doNothing().when(wishlistRepository).deleteByUserIdAndProductId(1L, 99L);
                  assertDoesNotThrow(() -> wishlistService.removeFromWishlist(1L, 99L));
        }
  }
