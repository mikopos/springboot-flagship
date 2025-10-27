package com.flagship.order.controller;

import com.flagship.order.model.Order;
import com.flagship.order.model.OrderItem;
import com.flagship.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Order Controller
 * <p>
 * REST API endpoints for order management operations. Provides CRUD operations for orders and order
 * management.
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  @PostMapping
  public ResponseEntity<Order> createOrder(@Valid @RequestBody Order order) {
    log.info("Creating new order for user: {}", order.getUserId());
    Order createdOrder = orderService.createOrder(order);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('ORDER_MANAGER') or @orderService.isOrderOwner(#id, authentication)")
  public ResponseEntity<Order> getOrderById(@PathVariable Long id,
      @AuthenticationPrincipal Jwt jwt) {
    log.debug("Getting order by ID: {}", id);

    Optional<Order> order = orderService.findById(id);
    return order.map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/number/{orderNumber}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('ORDER_MANAGER') or @orderService.isOrderOwnerByNumber(#orderNumber, authentication)")
  public ResponseEntity<Order> getOrderByNumber(@PathVariable String orderNumber,
      @AuthenticationPrincipal Jwt jwt) {
    log.debug("Getting order by number: {}", orderNumber);

    Optional<Order> order = orderService.findByOrderNumber(orderNumber);
    return order.map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/my-orders")
  public ResponseEntity<Page<Order>> getMyOrders(@AuthenticationPrincipal Jwt jwt, Pageable pageable) {
    String userId = jwt.getClaim("sub");
    log.debug("Getting orders for user: {} with pagination: {}", userId, pageable);

    Page<Order> orders = orderService.findByUserId(Long.valueOf(userId), pageable);
    return ResponseEntity.ok(orders);
  }

  @PutMapping("/{id}/status")
  @PreAuthorize("hasRole('ADMIN') or hasRole('ORDER_MANAGER')")
  public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id,
      @RequestParam Order.OrderStatus status) {
    log.info("Updating order status for order ID: {} to: {}", id, status);

    try {
      Order updatedOrder = orderService.updateOrderStatus(id, status);
      return ResponseEntity.ok(updatedOrder);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PutMapping("/{id}/payment-status")
  @PreAuthorize("hasRole('ADMIN') or hasRole('ORDER_MANAGER')")
  public ResponseEntity<Order> updatePaymentStatus(@PathVariable Long id,
      @RequestParam Order.PaymentStatus paymentStatus) {
    log.info("Updating payment status for order ID: {} to: {}", id, paymentStatus);

    try {
      Order updatedOrder = orderService.updatePaymentStatus(id, paymentStatus);
      return ResponseEntity.ok(updatedOrder);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/{id}/items")
  @PreAuthorize("hasRole('ADMIN') or hasRole('ORDER_MANAGER') or @orderService.isOrderOwner(#id, authentication)")
  public ResponseEntity<Order> addOrderItem(@PathVariable Long id,
      @Valid @RequestBody OrderItem item) {
    log.info("Adding item to order ID: {}", id);

    try {
      Order updatedOrder = orderService.addOrderItem(id, item);
      return ResponseEntity.ok(updatedOrder);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    } catch (IllegalStateException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @DeleteMapping("/{id}/items/{itemId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('ORDER_MANAGER') or @orderService.isOrderOwner(#id, authentication)")
  public ResponseEntity<Order> removeOrderItem(@PathVariable Long id,
      @PathVariable Long itemId) {
    log.info("Removing item from order ID: {}", id);

    try {
      Order updatedOrder = orderService.removeOrderItem(id, itemId);
      return ResponseEntity.ok(updatedOrder);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    } catch (IllegalStateException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PutMapping("/{id}/cancel")
  @PreAuthorize("hasRole('ADMIN') or hasRole('ORDER_MANAGER') or @orderService.isOrderOwner(#id, authentication)")
  public ResponseEntity<Order> cancelOrder(@PathVariable Long id) {
    log.info("Cancelling order ID: {}", id);

    try {
      Order updatedOrder = orderService.cancelOrder(id);
      return ResponseEntity.ok(updatedOrder);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    } catch (IllegalStateException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PutMapping("/{id}/tracking")
  @PreAuthorize("hasRole('ADMIN') or hasRole('ORDER_MANAGER')")
  public ResponseEntity<Order> updateTracking(@PathVariable Long id,
      @RequestParam String trackingNumber,
      @RequestParam(required = false) String shippingMethod) {
    log.info("Updating tracking for order ID: {}", id);

    try {
      Order updatedOrder = orderService.updateTracking(id, trackingNumber, shippingMethod);
      return ResponseEntity.ok(updatedOrder);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('ORDER_MANAGER')")
  public ResponseEntity<Page<Order>> getAllOrders(Pageable pageable) {
    log.debug("Getting all orders with pagination: {}", pageable);

    //TODO This would need to be implemented in OrderService with pagination support
    // For now, return empty page to avoid memory issues
    return ResponseEntity.ok(Page.empty());
  }

  @GetMapping("/status/{status}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('ORDER_MANAGER')")
  public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable Order.OrderStatus status) {
    log.debug("Getting orders by status: {}", status);

    List<Order> orders = orderService.findByStatus(status);
    return ResponseEntity.ok(orders);
  }

  @GetMapping("/date-range")
  @PreAuthorize("hasRole('ADMIN') or hasRole('ORDER_MANAGER')")
  public ResponseEntity<List<Order>> getOrdersByDateRange(@RequestParam LocalDateTime startDate,
      @RequestParam LocalDateTime endDate) {
    log.debug("Getting orders by date range: {} to {}", startDate, endDate);

    List<Order> orders = orderService.findByDateRange(startDate, endDate);
    return ResponseEntity.ok(orders);
  }

  @GetMapping("/health")
  public ResponseEntity<String> health() {
    return ResponseEntity.ok("Order service is healthy");
  }
}
