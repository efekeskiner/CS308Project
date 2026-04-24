package com.bookstore.service;

import com.bookstore.dto.ProductDto;
import com.bookstore.dto.ProductRequest;
import com.bookstore.model.Category;
import com.bookstore.model.Product;
import com.bookstore.repository.CategoryRepository;
import com.bookstore.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public Page<ProductDto> listProducts(String search, Long categoryId,
                                         String sortBy, String sortDir,
                                         int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 20;
        Pageable pageable = PageRequest.of(page, size, buildSort(sortBy, sortDir));
        String q = (search != null && !search.isBlank()) ? search.trim() : null;
        return productRepository.search(q, categoryId, pageable).map(ProductDto::new);
    }

    public ProductDto getById(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product " + id + " not found"));
        return new ProductDto(p);
    }

    @Transactional
    public ProductDto create(ProductRequest req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (req.getPrice() == null || req.getPrice().signum() < 0) {
            throw new IllegalArgumentException("Price must be non-negative");
        }
        if (req.getSerialNumber() != null
                && productRepository.existsBySerialNumber(req.getSerialNumber())) {
            throw new IllegalArgumentException("Serial number already exists: " + req.getSerialNumber());
        }
        Product p = new Product();
        applyRequest(p, req);
        if (p.getOriginalPrice() == null) p.setOriginalPrice(p.getPrice());
        if (p.getDiscountRate() == null) p.setDiscountRate(BigDecimal.ZERO);
        return new ProductDto(productRepository.save(p));
    }

    @Transactional
    public ProductDto update(Long id, ProductRequest req) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product " + id + " not found"));
        if (req.getSerialNumber() != null
                && !req.getSerialNumber().equals(p.getSerialNumber())
                && productRepository.existsBySerialNumber(req.getSerialNumber())) {
            throw new IllegalArgumentException("Serial number already exists: " + req.getSerialNumber());
        }
        applyRequest(p, req);
        return new ProductDto(productRepository.save(p));
    }

    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new NoSuchElementException("Product " + id + " not found");
        }
        productRepository.deleteById(id);
    }

    @Transactional
    public ProductDto updateStock(Long id, Integer quantity) {
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Quantity must be non-negative");
        }
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product " + id + " not found"));
        p.setQuantityInStock(quantity);
        return new ProductDto(productRepository.save(p));
    }

    private void applyRequest(Product p, ProductRequest req) {
        if (req.getName() != null) p.setName(req.getName());
        if (req.getModel() != null) p.setModel(req.getModel());
        if (req.getSerialNumber() != null) p.setSerialNumber(req.getSerialNumber());
        if (req.getDescription() != null) p.setDescription(req.getDescription());
        if (req.getQuantityInStock() != null) p.setQuantityInStock(req.getQuantityInStock());
        if (req.getPrice() != null) p.setPrice(req.getPrice());
        if (req.getWarrantyStatus() != null) p.setWarrantyStatus(req.getWarrantyStatus());
        if (req.getDistributorInfo() != null) p.setDistributorInfo(req.getDistributorInfo());
        if (req.getImageUrl() != null) p.setImageUrl(req.getImageUrl());
        if (req.getCategoryId() != null) {
            Category cat = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Category " + req.getCategoryId() + " not found"));
            p.setCategory(cat);
        }
    }

    private Sort buildSort(String sortBy, String sortDir) {
        Sort.Direction dir = "desc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        if (sortBy == null || sortBy.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        switch (sortBy.toLowerCase()) {
            case "price":
                return Sort.by(dir, "price");
            case "name":
                return Sort.by(dir, "name");
            case "popularity":
                // TODO: switch to rating/order-count aggregate once ratings are persisted
                return Sort.by(Sort.Direction.DESC, "createdAt");
            default:
                return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }
}
