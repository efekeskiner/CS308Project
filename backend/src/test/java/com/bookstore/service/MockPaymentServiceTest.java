package com.bookstore.service;

import com.bookstore.dto.CreditCardRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
  class MockPaymentServiceTest {

    @InjectMocks
        private MockPaymentService mockPaymentService;

    @Test
        void charge_throwsWhenCardIsNull() {
                  assertThrows(IllegalArgumentException.class,
                                               () -> mockPaymentService.charge(null, BigDecimal.TEN));
        }

    @Test
        void charge_throwsWhenCardNumberIsNull() {
                  CreditCardRequest card = new CreditCardRequest();
                  card.setNumber(null);
                  assertThrows(IllegalArgumentException.class,
                                               () -> mockPaymentService.charge(card, BigDecimal.TEN));
        }

    @Test
        void charge_throwsWhenCardNumberIsBlank() {
                  CreditCardRequest card = new CreditCardRequest();
                  card.setNumber("   ");
                  assertThrows(IllegalArgumentException.class,
                                               () -> mockPaymentService.charge(card, BigDecimal.TEN));
        }

    @Test
        void charge_returnsTrueForValidCard() {
                  CreditCardRequest card = new CreditCardRequest();
                  card.setNumber("4111111111111111");
                  assertTrue(mockPaymentService.charge(card, BigDecimal.valueOf(99.99)));
        }
  }
