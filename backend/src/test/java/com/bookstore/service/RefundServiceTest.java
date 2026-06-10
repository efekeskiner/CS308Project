package com.bookstore.service;

import com.bookstore.model.Order;
import com.bookstore.model.OrderItem;
import com.bookstore.model.Refund;
import com.bookstore.model.User;
import com.bookstore.repository.OrderRepository;
import com.bookstore.repository.RefundRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {

      @Mock
      private RefundRepository refundRepository;

      @Mock
      private OrderRepository orderRepository;

      @InjectMocks
      private RefundService refundService;

      private Order buildOrder(long id, String status) {
                Order order = new Order();
                ReflectionTestUtils.setField(order, "id", id);
                order.setStatus(status);
                User user = new User();
                ReflectionTestUtils.setField(user, "id", 1L);
                order.setUser(user);
                return order;
            }

      @Test
      void requestRefund_createsRefund_whenOrderIsDelivered() {
                Order order = buildOrder(1L, "DELIVERED");

                when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
                when(refundRepository.save(any(Refund.class))).thenAnswer(inv -> inv.getArgument(0));

                Refund result = refundService.requestRefund(1L, 1L, "Wrong item");

                assertNotNull(result);
                assertEquals("PENDING", result.getStatus());
                verify(refundRepository).save(any(Refund.class));
            }

      @Test
      void requestRefund_throwsException_whenOrderNotFound() {
                when(orderRepository.findById(99L)).thenReturn(Optional.empty());

                assertThrows(RuntimeException.class,
                                             () -> refundService.requestRefund(99L, 1L, "reason"));
            }

      @Test
      void requestRefund_throwsException_whenOrderNotDelivered() {
                Order order = buildOrder(2L, "PROCESSING");

                when(orderRepository.findById(2L)).thenReturn(Optional.of(order));

                assertThrows(RuntimeException.class,
                                             () -> refundService.requestRefund(2L, 1L, "reason"));
            }

      @Test
      void approveRefund_updatesStatusToApproved() {
                Refund refund = new Refund();
                ReflectionTestUtils.setField(refund, "id", 1L);
                refund.setStatus("PENDING");

                when(refundRepository.findById(1L)).thenReturn(Optional.of(refund));
                when(refundRepository.save(any(Refund.class))).thenAnswer(inv -> inv.getArgument(0));

                Refund result = refundService.approveRefund(1L);

                assertEquals("APPROVED", result.getStatus());
                verify(refundRepository).save(refund);
            }

      @Test
      void approveRefund_throwsException_whenRefundNotFound() {
                when(refundRepository.findById(99L)).thenReturn(Optional.empty());

                assertThrows(RuntimeException.class, () -> refundService.approveRefund(99L));
            }

      @Test
      void rejectRefund_updatesStatusToRejected() {
                Refund refund = new Refund();
                ReflectionTestUtils.setField(refund, "id", 2L);
                refund.setStatus("PENDING");

                when(refundRepository.findById(2L)).thenReturn(Optional.of(refund));
                when(refundRepository.save(any(Refund.class))).thenAnswer(inv -> inv.getArgument(0));

                Refund result = refundService.rejectRefund(2L);

                assertEquals("REJECTED", result.getStatus());
                verify(refundRepository).save(refund);
            }

      @Test
      void getPendingRefunds_returnsPendingList() {
                Refund r1 = new Refund();
                r1.setStatus("PENDING");

                when(refundRepository.findByStatus("PENDING")).thenReturn(List.of(r1));

                List<Refund> result = refundService.getPendingRefunds();

                assertEquals(1, result.size());
                assertEquals("PENDING", result.get(0).getStatus());
            }
  }
