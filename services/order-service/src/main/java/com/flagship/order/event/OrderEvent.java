package com.flagship.order.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Order Event
 * <p>
 * Represents an order-related event that can be published to Kafka. Used for event-driven
 * communication between microservices.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

  private Long orderId;
  private String orderNumber;
  private Long userId;
  private OrderEventType eventType;
  private LocalDateTime timestamp;
  private String metadata;

  private BigDecimal totalAmount;
  private String currency;
  private String status;
  private String paymentStatus;
  private String shippingAddress;
  private String billingAddress;
  private String paymentMethod;
  private String shippingMethod;
  private String trackingNumber;

  private List<OrderItemEvent> items;

  public enum OrderEventType {
    ORDER_CREATED,
    ORDER_CONFIRMED,
    ORDER_PROCESSING,
    ORDER_SHIPPED,
    ORDER_DELIVERED,
    ORDER_CANCELLED,
    ORDER_REFUNDED,
    PAYMENT_PENDING,
    PAYMENT_PAID,
    PAYMENT_FAILED,
    PAYMENT_REFUNDED,
    ITEM_ADDED,
    ITEM_REMOVED,
    ITEM_UPDATED,
    STATUS_CHANGED,
    ADDRESS_UPDATED,
    TRACKING_UPDATED
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class OrderItemEvent {

    private Long productId;
    private String productName;
    private String productSku;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private String productImageUrl;
    private String productDescription;
  }
}
