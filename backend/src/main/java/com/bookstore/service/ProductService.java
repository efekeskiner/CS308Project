package com.bookstore.service;

import com.bookstore.dto.ProductDto;
import com.bookstore.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
  public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
              this.productRepository = productRepository;
    }

    public List<ProductDto> getAllProducts() {
              return productRepository.findAll()
                                .stream()
                                .map(ProductDto::new)
                                .collect(Collectors.toList());
    }

    public List<ProductDto> searchProducts(String keyword) {
              return productRepository.findByNameContainingIgnoreCase(keyword)
                                .stream()
                                .map(ProductDto::new)
                                .collect(Collectors.toList());
    }

    public List<ProductDto> getProductsByCategory(String category) {
              return productRepository.findByCategory(category)
                                .stream()
                                .map(ProductDto::new)
                                .collect(Collectors.toList());
    }
  }
