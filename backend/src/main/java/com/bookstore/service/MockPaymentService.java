package com.bookstore.service;

import com.bookstore.dto.CreditCardRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class MockPaymentService {

    public boolean charge(CreditCardRequest card, BigDecimal amount) {
        if (card == null || card.getNumber() == null || card.getNumber().isBlank()) {
            throw new IllegalArgumentException("Credit card information is required");
        }
        // Mock: all non-null card inputs are accepted
        return true;
    }
}
