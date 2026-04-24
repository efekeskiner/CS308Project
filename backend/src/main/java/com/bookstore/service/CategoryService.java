package com.bookstore.service;

import com.bookstore.dto.CategoryDto;
import com.bookstore.dto.CategoryRequest;
import com.bookstore.model.Category;
import com.bookstore.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryDto> getAll() {
        return categoryRepository.findAll().stream()
                .map(CategoryDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryDto create(CategoryRequest req) {
        String name = requireName(req);
        if (categoryRepository.existsByName(name)) {
            throw new IllegalArgumentException("Category already exists: " + name);
        }
        return new CategoryDto(categoryRepository.save(new Category(name)));
    }

    @Transactional
    public CategoryDto update(Long id, CategoryRequest req) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Category " + id + " not found"));
        String name = requireName(req);
        if (!cat.getName().equals(name) && categoryRepository.existsByName(name)) {
            throw new IllegalArgumentException("Category already exists: " + name);
        }
        cat.setName(name);
        return new CategoryDto(categoryRepository.save(cat));
    }

    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new NoSuchElementException("Category " + id + " not found");
        }
        categoryRepository.deleteById(id);
    }

    private String requireName(CategoryRequest req) {
        if (req == null || req.getName() == null || req.getName().isBlank()) {
            throw new IllegalArgumentException("Category name is required");
        }
        return req.getName().trim();
    }
}
