package com.bookstore.controller;

import com.bookstore.dto.ProductDto;
import com.bookstore.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
  @RequestMapping("/api/products")
  @CrossOrigin(origins = "http://localhost:3000")
  public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
              this.productService = productService;
    }

    @GetMapping
        public ResponseEntity<List<ProductDto>> getProducts(
                      @RequestParam(required = false) String search,
                      @RequestParam(required = false) String category) {

            List<ProductDto> result;

            if (search != null && !search.isBlank()) {
                          result = productService.searchProducts(search);
            } else if (category != null && !category.isBlank()) {
                          result = productService.getProductsByCategory(category);
            } else {
                          result = productService.getAllProducts();
            }

            return ResponseEntity.ok(result);
        }

    @GetMapping("/{id}")
        public ResponseEntity<?> getProductById(@PathVariable Long id) {
                  return productService.getAllProducts()
                                    .stream()
                                    .filter(p -> p.getId().equals(id))
                                    .findFirst()
                                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                                    .orElse(ResponseEntity.notFound().build());
        }
  }
