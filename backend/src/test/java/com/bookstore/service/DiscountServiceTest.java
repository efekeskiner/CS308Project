package com.bookstore.service;

import com.bookstore.model.Discount;
import com.bookstore.repository.DiscountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
  class DiscountServiceTest {

    @Mock
        private DiscountRepository discountRepository;

    @InjectMocks
        private DiscountService discountService;

    @Test
        void createDiscount_savesAndReturnsDiscount() {
                  Discount discount = new Discount();
                  discount.setCode("SAVE10");
                  discount.setPercentage(BigDecimal.valueOf(10));
                  discount.setActive(true);
                  discount.setExpiryDate(LocalDateTime.now().plusDays(7));

            when(discountRepository.save(any(Discount.class))).thenReturn(discount);

            Discount result = discountService.createDiscount(discount);

            assertNotNull(result);
                  assertEquals("SAVE10", result.getCode());
                  verify(discountRepository).save(discount);
        }

    @Test
        void getActiveDiscounts_returnsOnlyActiveDiscounts() {
                  Discount active = new Discount();
                  active.setActive(true);

            when(discountRepository.findByActiveTrue()).thenReturn(List.of(active));

            List<Discount> result = discountService.getActiveDiscounts();

            assertEquals(1, result.size());
                  assertTrue(result.get(0).isActive());
        }

    @Test
        void getActiveDiscounts_returnsEmptyListWhenNoneActive() {
                  when(discountRepository.findByActiveTrue()).thenReturn(List.of());

            List<Discount> result = discountService.getActiveDiscounts();

            assertTrue(result.isEmpty());
        }

    @Test
        void getDiscountByCode_returnsDiscount_whenCodeExists() {
                  Discount discount = new Discount();
                  discount.setCode("WELCOME");

            when(discountRepository.findByCode("WELCOME")).thenReturn(Optional.of(discount));

            Optional<Discount> result = discountService.getDiscountByCode("WELCOME");

            assertTrue(result.isPresent());
                  assertEquals("WELCOME", result.get().getCode());
        }

    @Test
        void getDiscountByCode_returnsEmpty_whenCodeNotFound() {
                  when(discountRepository.findByCode("NOTEXIST")).thenReturn(Optional.empty());

            Optional<Discount> result = discountService.getDiscountByCode("NOTEXIST");

            assertFalse(result.isPresent());
        }

    @Test
        void deactivateDiscount_setsActiveFalseAndSaves() {
                  Discount discount = new Discount();
                  ReflectionTestUtils.setField(discount, "id", 1L);
                  discount.setActive(true);

            when(discountRepository.findById(1L)).thenReturn(Optional.of(discount));
                  when(discountRepository.save(any(Discount.class))).thenAnswer(inv -> inv.getArgument(0));

            discountService.deactivateDiscount(1L);

            assertFalse(discount.isActive());
                  verify(discountRepository).save(discount);
        }

    @Test
        void deactivateDiscount_throwsException_whenDiscountNotFound() {
                  when(discountRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> discountService.deactivateDiscount(99L));
        }
  }
