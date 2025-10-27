package com.flagship.order.service;

import com.flagship.order.event.OrderEvent;
import com.flagship.order.model.Order;
import com.flagship.order.model.OrderItem;
import com.flagship.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Order Service
 * <p>
 * Business logic layer for order management operations. Handles order CRUD operations, status
 * management, and event publishing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

  private final OrderRepository orderRepository;
  private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
  private final OrderEventService orderEventService;

  public Order createOrder(Order order) {
    log.info("Creating new order for user: {}", order.getUserId());

    order.setOrderNumber(generateOrderNumber());

    order.setStatus(Order.OrderStatus.PENDING);
    order.setPaymentStatus(Order.PaymentStatus.PENDING);

    order.recalculateTotal();

    Order savedOrder = orderRepository.save(order);

    orderEventService.logEvent(savedOrder, com.flagship.order.model.OrderEvent.EventType.ORDER_CREATED,
        "Order created", null, null);

    publishOrderEvent(savedOrder, OrderEvent.OrderEventType.ORDER_CREATED);

    log.info("Order created successfully with ID: {} and number: {}",
        savedOrder.getId(), savedOrder.getOrderNumber());
    return savedOrder;
  }

  @Transactional(readOnly = true)
  public Optional<Order> findById(Long id) {
    return orderRepository.findById(id);
  }

  @Transactional(readOnly = true)
  public Optional<Order> findByOrderNumber(String orderNumber) {
    return orderRepository.findByOrderNumber(orderNumber);
  }

  @Transactional(readOnly = true)
  public List<Order> findByUserId(Long userId) {
    return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
  }

  @Transactional(readOnly = true)
  public Page<Order> findByUserId(Long userId, Pageable pageable) {
    return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
  }

  public Order updateOrderStatus(Long orderId, Order.OrderStatus newStatus) {
    log.info("Updating order status for order ID: {} to: {}", orderId, newStatus);

    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

    Order.OrderStatus previousStatus = order.getStatus();
    order.updateStatus(newStatus);

    Order savedOrder = orderRepository.save(order);

    orderEventService.logEvent(savedOrder, com.flagship.order.model.OrderEvent.EventType.STATUS_CHANGED,
        "Order status changed from " + previousStatus + " to " + newStatus,
        previousStatus.toString(), newStatus.toString());

    publishOrderEvent(savedOrder, OrderEvent.OrderEventType.STATUS_CHANGED);

    log.info("Order status updated successfully for order ID: {}", orderId);
    return savedOrder;
  }

  public Order updatePaymentStatus(Long orderId, Order.PaymentStatus newStatus) {
    log.info("Updating payment status for order ID: {} to: {}", orderId, newStatus);

    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

    Order.PaymentStatus previousStatus = order.getPaymentStatus();
    order.updatePaymentStatus(newStatus);

    Order savedOrder = orderRepository.save(order);

    orderEventService.logEvent(savedOrder, com.flagship.order.model.OrderEvent.EventType.PAYMENT_PAID,
        "Payment status changed from " + previousStatus + " to " + newStatus,
        previousStatus.toString(), newStatus.toString());

    OrderEvent.OrderEventType eventType = switch (newStatus) {
      case PAID -> OrderEvent.OrderEventType.PAYMENT_PAID;
      case FAILED -> OrderEvent.OrderEventType.PAYMENT_FAILED;
      case REFUNDED -> OrderEvent.OrderEventType.PAYMENT_REFUNDED;
      default -> OrderEvent.OrderEventType.PAYMENT_PENDING;
    };

    publishOrderEvent(savedOrder, eventType);

    log.info("Payment status updated successfully for order ID: {}", orderId);
    return savedOrder;
  }

  public Order addOrderItem(Long orderId, OrderItem item) {
    log.info("Adding item to order ID: {}", orderId);

    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

    if (!order.canBeCancelled()) {
      throw new IllegalStateException("Cannot modify order in status: " + order.getStatus());
    }

    order.addItem(item);
    Order savedOrder = orderRepository.save(order);

    orderEventService.logEvent(savedOrder, com.flagship.order.model.OrderEvent.EventType.ITEM_ADDED,
        "Item added: " + item.getProductName(), null, null);

    publishOrderEvent(savedOrder, OrderEvent.OrderEventType.ITEM_ADDED);

    log.info("Item added successfully to order ID: {}", orderId);
    return savedOrder;
  }

  public Order removeOrderItem(Long orderId, Long itemId) {
    log.info("Removing item from order ID: {}", orderId);

    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

    if (!order.canBeCancelled()) {
      throw new IllegalStateException("Cannot modify order in status: " + order.getStatus());
    }

    OrderItem itemToRemove = order.getItems().stream()
        .filter(item -> item.getId().equals(itemId))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Item not found with ID: " + itemId));

    order.removeItem(itemToRemove);
    Order savedOrder = orderRepository.save(order);

    orderEventService.logEvent(savedOrder, com.flagship.order.model.OrderEvent.EventType.ITEM_REMOVED,
        "Item removed: " + itemToRemove.getProductName(), null, null);

    publishOrderEvent(savedOrder, OrderEvent.OrderEventType.ITEM_REMOVED);

    log.info("Item removed successfully from order ID: {}", orderId);
    return savedOrder;
  }

  public Order cancelOrder(Long orderId) {
    log.info("Cancelling order ID: {}", orderId);

    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

    if (!order.canBeCancelled()) {
      throw new IllegalStateException("Cannot cancel order in status: " + order.getStatus());
    }

    order.updateStatus(Order.OrderStatus.CANCELLED);
    Order savedOrder = orderRepository.save(order);

    orderEventService.logEvent(savedOrder, com.flagship.order.model.OrderEvent.EventType.ORDER_CANCELLED,
        "Order cancelled", null, null);

    publishOrderEvent(savedOrder, OrderEvent.OrderEventType.ORDER_CANCELLED);

    log.info("Order cancelled successfully with ID: {}", orderId);
    return savedOrder;
  }

  public Order updateTracking(Long orderId, String trackingNumber, String shippingMethod) {
    log.info("Updating tracking for order ID: {}", orderId);

    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

    order.setTrackingNumber(trackingNumber);
    order.setShippingMethod(shippingMethod);

    Order savedOrder = orderRepository.save(order);

    orderEventService.logEvent(savedOrder, com.flagship.order.model.OrderEvent.EventType.TRACKING_UPDATED,
        "Tracking updated: " + trackingNumber, null, null);

    publishOrderEvent(savedOrder, OrderEvent.OrderEventType.TRACKING_UPDATED);

    log.info("Tracking updated successfully for order ID: {}", orderId);
    return savedOrder;
  }

  @Transactional(readOnly = true)
  public List<Order> findByStatus(Order.OrderStatus status) {
    return orderRepository.findByStatusOrderByCreatedAtDesc(status);
  }

  @Transactional(readOnly = true)
  public List<Order> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
    return orderRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate);
  }

  private String generateOrderNumber() {
    return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
  }

  private void publishOrderEvent(Order order, OrderEvent.OrderEventType eventType) {
    try {
      // Create lightweight event with only essential data
      OrderEvent event = OrderEvent.builder()
          .orderId(order.getId())
          .orderNumber(order.getOrderNumber())
          .userId(order.getUserId())
          .eventType(eventType)
          .timestamp(LocalDateTime.now())
          .totalAmount(order.getTotalAmount())
          .currency(order.getCurrency())
          .status(order.getStatus().toString())
          .paymentStatus(order.getPaymentStatus().toString())
          .build();

      kafkaTemplate.send("order-events", event);
      log.debug("Published order event: {} for order: {}", eventType, order.getId());
    } catch (Exception e) {
      log.error("Failed to publish order event: {} for order: {}", eventType, order.getId(), e);
    }
  }
}
