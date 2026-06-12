package com.bookstore.service;

import com.bookstore.dto.RefundDto;
import com.bookstore.dto.RefundRequestCreate;
import com.bookstore.model.Order;
import com.bookstore.model.OrderItem;
import com.bookstore.model.OrderStatus;
import com.bookstore.model.Product;
import com.bookstore.model.RefundRequest;
import com.bookstore.model.RefundStatus;
import com.bookstore.model.Role;
import com.bookstore.model.User;
import com.bookstore.repository.OrderItemRepository;
import com.bookstore.repository.ProductRepository;
import com.bookstore.repository.RefundRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {

    @Mock private RefundRequestRepository refundRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private EmailService emailService;

    @InjectMocks private RefundService refundService;

    private User user(long id) {
        User u = new User();
        ReflectionTestUtils.setField(u, "id", id);
        u.setName("Demo Customer");
        u.setEmail("customer@test.com");
        u.setRole(Role.CUSTOMER);
        return u;
    }

    /** A DELIVERED order item owned by {@code owner}, placed {@code daysAgo} days ago. */
    private OrderItem deliveredItem(long itemId, User owner, int daysAgo) {
        Product product = new Product();
        ReflectionTestUtils.setField(product, "id", 10L);
        product.setName("1984");
        product.setQuantityInStock(5);

        Order order = new Order();
        ReflectionTestUtils.setField(order, "id", 100L);
        order.setUser(owner);
        order.setStatus(OrderStatus.DELIVERED);
        ReflectionTestUtils.setField(order, "createdAt", LocalDateTime.now().minusDays(daysAgo));

        OrderItem item = new OrderItem(order, product, 1, BigDecimal.valueOf(10.50));
        ReflectionTestUtils.setField(item, "id", itemId);
        return item;
    }

    private RefundRequestCreate request(long itemId) {
        RefundRequestCreate req = new RefundRequestCreate();
        req.setOrderItemId(itemId);
        req.setReason("Damaged");
        return req;
    }

    @Test
    void requestRefund_createsPendingRequest_whenDeliveredAndWithinWindow() {
        User caller = user(1L);
        OrderItem item = deliveredItem(50L, caller, 5);

        when(orderItemRepository.findById(50L)).thenReturn(Optional.of(item));
        when(refundRepository.existsByOrderItemIdAndStatusIn(eq(50L), any())).thenReturn(false);
        when(refundRepository.save(any(RefundRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        RefundDto dto = refundService.requestRefund(caller, request(50L));

        assertEquals(RefundStatus.PENDING, dto.getStatus());
        assertEquals(0, BigDecimal.valueOf(10.50).compareTo(dto.getRefundAmount()));
        verify(refundRepository).save(any(RefundRequest.class));
    }

    @Test
    void requestRefund_throws_whenOrderItemNotFound() {
        when(orderItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> refundService.requestRefund(user(1L), request(99L)));
    }

    @Test
    void requestRefund_throwsSecurity_whenNotOwner() {
        User owner = user(1L);
        User attacker = user(2L);
        when(orderItemRepository.findById(50L)).thenReturn(Optional.of(deliveredItem(50L, owner, 5)));

        assertThrows(SecurityException.class,
                () -> refundService.requestRefund(attacker, request(50L)));
    }

    @Test
    void requestRefund_throws_whenOrderNotDelivered() {
        User caller = user(1L);
        OrderItem item = deliveredItem(50L, caller, 5);
        item.getOrder().setStatus(OrderStatus.PROCESSING);
        when(orderItemRepository.findById(50L)).thenReturn(Optional.of(item));

        assertThrows(ResponseStatusException.class,
                () -> refundService.requestRefund(caller, request(50L)));
    }

    @Test
    void requestRefund_throws_whenOutsideRefundWindow() {
        User caller = user(1L);
        when(orderItemRepository.findById(50L)).thenReturn(Optional.of(deliveredItem(50L, caller, 45)));

        assertThrows(ResponseStatusException.class,
                () -> refundService.requestRefund(caller, request(50L)));
    }

    @Test
    void approve_setsApproved_restocksAndNotifiesCustomer() {
        User caller = user(1L);
        OrderItem item = deliveredItem(50L, caller, 5);
        Product product = item.getProduct();
        RefundRequest refund = new RefundRequest(item, caller, BigDecimal.valueOf(10.50), "Damaged");
        ReflectionTestUtils.setField(refund, "id", 7L);

        User resolver = user(99L);
        resolver.setRole(Role.SALES_MANAGER);

        when(refundRepository.findById(7L)).thenReturn(Optional.of(refund));
        when(productRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(product));
        when(refundRepository.save(any(RefundRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        RefundDto dto = refundService.approve(resolver, 7L);

        assertEquals(RefundStatus.APPROVED, dto.getStatus());
        assertEquals(6, product.getQuantityInStock()); // 5 + 1 restocked
        verify(emailService).sendRefundApprovedNotification(eq("customer@test.com"), any(), eq("1984"), any());
    }

    @Test
    void approve_throws_whenRefundAlreadyResolved() {
        User caller = user(1L);
        RefundRequest refund = new RefundRequest(deliveredItem(50L, caller, 5), caller, BigDecimal.valueOf(10.50), null);
        refund.setStatus(RefundStatus.APPROVED);
        when(refundRepository.findById(7L)).thenReturn(Optional.of(refund));

        assertThrows(ResponseStatusException.class, () -> refundService.approve(user(99L), 7L));
    }

    @Test
    void reject_setsRejected_withoutRestocking() {
        User caller = user(1L);
        RefundRequest refund = new RefundRequest(deliveredItem(50L, caller, 5), caller, BigDecimal.valueOf(10.50), null);
        ReflectionTestUtils.setField(refund, "id", 8L);
        when(refundRepository.findById(8L)).thenReturn(Optional.of(refund));
        when(refundRepository.save(any(RefundRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        RefundDto dto = refundService.reject(user(99L), 8L);

        assertEquals(RefundStatus.REJECTED, dto.getStatus());
        verifyNoInteractions(productRepository);
    }
}
