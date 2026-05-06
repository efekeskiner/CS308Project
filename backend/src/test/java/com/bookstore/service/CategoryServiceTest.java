package com.bookstore.service;

import com.bookstore.dto.CategoryDto;
import com.bookstore.dto.CategoryRequest;
import com.bookstore.model.Category;
import com.bookstore.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
  class CategoryServiceTest {

    @Mock
        private CategoryRepository categoryRepository;

    @InjectMocks
        private CategoryService categoryService;

    // ── getAll ────────────────────────────────────────────────────────────────

    @Test
        void getAll_returnsEmptyListWhenNoCategoriesExist() {
                  when(categoryRepository.findAll()).thenReturn(List.of());
                  var result = categoryService.getAll();
                  assertNotNull(result);
                  assertTrue(result.isEmpty());
        }

    @Test
        void getAll_returnsAllCategories() {
                  Category cat = new Category("Fiction");
                  when(categoryRepository.findAll()).thenReturn(List.of(cat));
                  var result = categoryService.getAll();
                  assertEquals(1, result.size());
        }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
        void create_throwsWhenNameIsNull() {
                  CategoryRequest req = new CategoryRequest();
                  req.setName(null);
                  assertThrows(IllegalArgumentException.class,
                                               () -> categoryService.create(req));
        }

    @Test
        void create_throwsWhenNameIsBlank() {
                  CategoryRequest req = new CategoryRequest();
                  req.setName("   ");
                  assertThrows(IllegalArgumentException.class,
                                               () -> categoryService.create(req));
        }

    @Test
        void create_throwsWhenCategoryAlreadyExists() {
                  CategoryRequest req = new CategoryRequest();
                  req.setName("Fiction");
                  when(categoryRepository.existsByName("Fiction")).thenReturn(true);
                  assertThrows(IllegalArgumentException.class,
                                               () -> categoryService.create(req));
        }

    @Test
        void create_savesCategoryAndReturnsDto() {
                  CategoryRequest req = new CategoryRequest();
                  req.setName("Science");
                  when(categoryRepository.existsByName("Science")).thenReturn(false);
                  Category saved = new Category("Science");
                  saved.setId(1L);
                  when(categoryRepository.save(any())).thenReturn(saved);

            var dto = categoryService.create(req);
                  assertNotNull(dto);
                  assertEquals("Science", dto.getName());
                  verify(categoryRepository).save(any());
        }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
        void update_throwsWhenCategoryNotFound() {
                  when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
                  CategoryRequest req = new CategoryRequest();
                  req.setName("NewName");
                  assertThrows(NoSuchElementException.class,
                                               () -> categoryService.update(99L, req));
        }

    @Test
        void update_throwsWhenNewNameAlreadyTakenByAnotherCategory() {
                  Category existing = new Category("OldName");
                  existing.setId(1L);
                  when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));

            CategoryRequest req = new CategoryRequest();
                  req.setName("Taken");
                  when(categoryRepository.existsByName("Taken")).thenReturn(true);

            assertThrows(IllegalArgumentException.class,
                                         () -> categoryService.update(1L, req));
        }

    @Test
        void update_successfullyUpdatesName() {
                  Category existing = new Category("OldName");
                  existing.setId(1L);
                  when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));

            CategoryRequest req = new CategoryRequest();
                  req.setName("NewName");
                  when(categoryRepository.existsByName("NewName")).thenReturn(false);
                  when(categoryRepository.save(existing)).thenReturn(existing);

            var dto = categoryService.update(1L, req);
                  assertNotNull(dto);
                  assertEquals("NewName", dto.getName());
                  verify(categoryRepository).save(existing);
        }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
        void delete_throwsWhenCategoryNotFound() {
                  when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
                  assertThrows(NoSuchElementException.class,
                                               () -> categoryService.delete(99L));
        }

    @Test
        void delete_deletesExistingCategory() {
                  Category cat = new Category("Fiction");
                  cat.setId(5L);
                  when(categoryRepository.findById(5L)).thenReturn(Optional.of(cat));

            categoryService.delete(5L);
                  verify(categoryRepository).delete(cat);
        }
  }
