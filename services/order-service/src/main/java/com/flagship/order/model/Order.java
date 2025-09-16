package com.flagship.order.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order Entity
 * <p>
 * Represents an order in the e-commerce system. This entity manages the complete order lifecycle
 * from creation to completion.
 */
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_user_id", columnList = "userId"),
    @Index(name = "idx_order_status", columnList = "status"),
    @Index(name = "idx_order_created_at", columnList = "createdAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "order_number", unique = true, nullable = false)
  @NotBlank(message = "Order number is required")
  private String orderNumber;

  @Column(name = "user_id", nullable = false)
  @NotNull(message = "User ID is required")
  private Long userId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  @Builder.Default
  private OrderStatus status = OrderStatus.PENDING;

  @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
  @NotNull(message = "Total amount is required")
  @Positive(message = "Total amount must be positive")
  private BigDecimal totalAmount;

  @Column(name = "currency", nullable = false)
  @NotBlank(message = "Currency is required")
  @Builder.Default
  private String currency = "USD";

  @Column(name = "shipping_address", columnDefinition = "TEXT")
  private String shippingAddress;

  @Column(name = "billing_address", columnDefinition = "TEXT")
  private String billingAddress;

  @Column(name = "payment_method")
  private String paymentMethod;

  @Column(name = "payment_status")
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private PaymentStatus paymentStatus = PaymentStatus.PENDING;

  @Column(name = "shipping_method")
  private String shippingMethod;

  @Column(name = "tracking_number")
  private String trackingNumber;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  @Column(name = "estimated_delivery_date")
  private LocalDateTime estimatedDeliveryDate;

  @Column(name = "actual_delivery_date")
  private LocalDateTime actualDeliveryDate;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Builder.Default
  private List<OrderItem> items = new ArrayList<>();

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Builder.Default
  private List<OrderEvent> events = new ArrayList<>();

  public void addItem(OrderItem item) {
    items.add(item);
    item.setOrder(this);
    recalculateTotal();
  }

  public void removeItem(OrderItem item) {
    items.remove(item);
    item.setOrder(null);
    recalculateTotal();
  }

  public void recalculateTotal() {
    this.totalAmount = items.stream()
        .map(OrderItem::getTotalPrice)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public void updateStatus(OrderStatus newStatus) {
    this.status = newStatus;
    this.updatedAt = LocalDateTime.now();
  }

  public void updatePaymentStatus(PaymentStatus newStatus) {
    this.paymentStatus = newStatus;
    this.updatedAt = LocalDateTime.now();
  }

  public boolean canBeCancelled() {
    return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
  }

  public boolean isCompleted() {
    return status == OrderStatus.DELIVERED;
  }

  public boolean isPaid() {
    return paymentStatus == PaymentStatus.PAID;
  }

  public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED
  }

  public enum PaymentStatus {
    PENDING,
    PAID,
    FAILED,
    REFUNDED,
    PARTIALLY_REFUNDED
  }
}
