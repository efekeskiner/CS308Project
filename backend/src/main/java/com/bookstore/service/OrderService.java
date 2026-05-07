package com.bookstore.service;

import com.bookstore.dto.OrderDto;
import com.bookstore.dto.OrderItemRequest;
import com.bookstore.dto.PlaceOrderRequest;
import com.bookstore.model.*;
import com.bookstore.repository.DeliveryRepository;
import com.bookstore.repository.InvoiceRepository;
import com.bookstore.repository.OrderRepository;
import com.bookstore.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final DeliveryRepository deliveryRepository;
    private final MockPaymentService mockPaymentService;
    private final InvoiceService invoiceService;
    private final EmailService emailService;
    private final InvoiceRepository invoiceRepository;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        DeliveryRepository deliveryRepository,
                        MockPaymentService mockPaymentService,
                        InvoiceService invoiceService,
                        EmailService emailService,
                        InvoiceRepository invoiceRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.deliveryRepository = deliveryRepository;
        this.mockPaymentService = mockPaymentService;
        this.invoiceService = invoiceService;
        this.emailService = emailService;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public OrderDto placeOrder(User user, PlaceOrderRequest req) {
        if (req == null || req.getItems() == null || req.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PROCESSING);
        String address = (req.getDeliveryAddress() != null && !req.getDeliveryAddress().isBlank())
                ? req.getDeliveryAddress().trim()
                : user.getHomeAddress();
        order.setDeliveryAddress(address);

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest line : req.getItems()) {
            if (line.getProductId() == null || line.getQuantity() == null || line.getQuantity() <= 0) {
                throw new IllegalArgumentException("Each order item needs a valid productId and positive quantity");
            }
            Product p = productRepository.findByIdForUpdate(line.getProductId())
                    .orElseThrow(() -> new NoSuchElementException("Product " + line.getProductId() + " not found"));
            if (p.getQuantityInStock() == null || p.getQuantityInStock() < line.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for product " + p.getId() + " ('" + p.getName() + "')");
            }
            p.setQuantityInStock(p.getQuantityInStock() - line.getQuantity());
            productRepository.save(p);

            OrderItem item = new OrderItem(order, p, line.getQuantity(), p.getPrice());
            order.addItem(item);

            BigDecimal lineTotal = p.getPrice().multiply(BigDecimal.valueOf(line.getQuantity()));
            total = total.add(lineTotal);
        }

        order.setTotalPrice(total);
        Order saved = orderRepository.save(order);

        // one delivery record per order item (design doc §3.10)
        for (OrderItem item : saved.getItems()) {
            BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            Delivery delivery = new Delivery(user, item.getProduct(), item.getQuantity(),
                    lineTotal, address, saved);
            deliveryRepository.save(delivery);
        }

        // mock payment — always succeeds for non-null card input
        mockPaymentService.charge(req.getCreditCard(), total);

        // generate invoice PDF and persist record
        Invoice invoice = invoiceService.createInvoice(saved, user);

        // send email asynchronously — failure does not affect order
        emailService.sendInvoiceEmail(
                user.getEmail(), user.getName(),
                saved.getId(), saved.getTotalPrice(), invoice.getPdfPath());

        return new OrderDto(saved, invoice.getId());
    }

    @Transactional
    public OrderDto cancelOrder(User user, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order " + orderId + " not found"));
        assertCanManage(order, user);
        if (order.getStatus() != OrderStatus.PROCESSING) {
            throw new IllegalStateException("Only PROCESSING orders can be cancelled (current: " + order.getStatus() + ")");
        }
        for (OrderItem item : order.getItems()) {
            Product p = productRepository.findByIdForUpdate(item.getProduct().getId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Product " + item.getProduct().getId() + " not found"));
            p.setQuantityInStock(p.getQuantityInStock() + item.getQuantity());
            productRepository.save(p);
        }
        order.setStatus(OrderStatus.CANCELLED);
        // mark any deliveries as completed (closed out) — prevents PM from advancing cancelled orders
        List<Delivery> deliveries = deliveryRepository.findByOrderId(order.getId());
        for (Delivery d : deliveries) {
            d.setIsCompleted(true);
            deliveryRepository.save(d);
        }
        return new OrderDto(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderDto getById(User user, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order " + orderId + " not found"));
        assertCanView(order, user);
        Long invoiceId = invoiceRepository.findByOrderId(order.getId()).map(Invoice::getId).orElse(null);
        return new OrderDto(order, invoiceId, deliveryRepository.findByOrderId(order.getId()));
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> list(User user, int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 20;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orders;
        if (isManager(user)) {
            orders = orderRepository.findAll(pageable);
        } else {
            orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
        }
        return orders.map(o -> {
            Long invoiceId = invoiceRepository.findByOrderId(o.getId()).map(Invoice::getId).orElse(null);
            return new OrderDto(o, invoiceId, deliveryRepository.findByOrderId(o.getId()));
        });
    }

    private void assertCanView(Order order, User user) {
        if (isManager(user)) return;
        if (order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You do not have access to this order");
        }
    }

    private void assertCanManage(Order order, User user) {
        // only the owning customer can cancel; managers may also cancel
        if (isManager(user)) return;
        if (order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You do not have access to this order");
        }
    }

    private boolean isManager(User user) {
        return user.getRole() == Role.PRODUCT_MANAGER || user.getRole() == Role.SALES_MANAGER;
    }
}
