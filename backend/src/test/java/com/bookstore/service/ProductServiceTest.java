package com.bookstore.service;

import com.bookstore.model.Product;
import com.bookstore.repository.CategoryRepository;
import com.bookstore.repository.ProductRepository;
import com.bookstore.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void getById_throwsWhenProductNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> productService.getById(999L));
    }

    @Test
    void getById_returnsProductDto_whenFound() {
        Product product = new Product();
        product.setName("Test Book");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(reviewRepository.findAverageScoreByProductId(1L)).thenReturn(null);
        when(reviewRepository.findCountByProductId(1L)).thenReturn(0L);

        var dto = productService.getById(1L);

        assertNotNull(dto);
        assertEquals("Test Book", dto.getName());
    }
}
