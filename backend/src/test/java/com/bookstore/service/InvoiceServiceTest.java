package com.bookstore.service;

import com.bookstore.dto.InvoiceDto;
import com.bookstore.model.Invoice;
import com.bookstore.model.Order;
import com.bookstore.model.Role;
import com.bookstore.model.User;
import com.bookstore.repository.InvoiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock private InvoiceRepository invoiceRepository;

    @InjectMocks private InvoiceService invoiceService;

    private User user(long id, Role role) {
        User u = new User();
        ReflectionTestUtils.setField(u, "id", id);
        u.setName("Test User");
        u.setEmail("customer@test.com");
        u.setRole(role);
        return u;
    }

    private Invoice invoiceFor(User owner, long orderId) {
        Order order = new Order();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setUser(owner);
        order.setTotalPrice(BigDecimal.valueOf(59.98));
        Invoice invoice = new Invoice(order, "./invoices/invoice-1.pdf");
        ReflectionTestUtils.setField(invoice, "id", 1L);
        return invoice;
    }

    @Test
    void getInvoice_returnsInvoice_forOwningCustomer() {
        User owner = user(1L, Role.CUSTOMER);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoiceFor(owner, 100L)));

        Invoice result = invoiceService.getInvoice(1L, owner);

        assertNotNull(result);
        assertEquals(100L, result.getOrder().getId());
    }

    @Test
    void getInvoice_allowsManager_evenIfNotOwner() {
        User owner = user(1L, Role.CUSTOMER);
        User manager = user(99L, Role.SALES_MANAGER);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoiceFor(owner, 100L)));

        Invoice result = invoiceService.getInvoice(1L, manager);

        assertNotNull(result);
    }

    @Test
    void getInvoice_throwsSecurityException_forOtherCustomer() {
        User owner = user(1L, Role.CUSTOMER);
        User attacker = user(2L, Role.CUSTOMER);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoiceFor(owner, 100L)));

        assertThrows(SecurityException.class, () -> invoiceService.getInvoice(1L, attacker));
    }

    @Test
    void getInvoice_throws_whenNotFound() {
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> invoiceService.getInvoice(99L, user(1L, Role.CUSTOMER)));
    }

    @Test
    void listInvoices_mapsInvoicesToDtos_forGivenRange() {
        User owner = user(1L, Role.CUSTOMER);
        when(invoiceRepository.findByCreatedAtBetween(any(), any()))
                .thenReturn(List.of(invoiceFor(owner, 100L)));

        List<InvoiceDto> result = invoiceService.listInvoices("2026-01-01", "2026-12-31");

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getOrderId());
        assertEquals("customer@test.com", result.get(0).getCustomerEmail());
    }

    @Test
    void listInvoices_usesDefaultRange_whenDatesBlank() {
        when(invoiceRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of());

        List<InvoiceDto> result = invoiceService.listInvoices(null, "");

        assertTrue(result.isEmpty());
        verify(invoiceRepository).findByCreatedAtBetween(any(), any());
    }
}
