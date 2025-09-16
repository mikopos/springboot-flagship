package com.flagship.order.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Order Event Entity
 * <p>
 * Tracks order events and state changes for audit and analytics purposes. This entity stores
 * information about order lifecycle events.
 */
@Entity
@Table(name = "order_events", indexes = {
    @Index(name = "idx_order_event_order_id", columnList = "order_id"),
    @Index(name = "idx_order_event_type", columnList = "event_type"),
    @Index(name = "idx_order_event_timestamp", columnList = "timestamp")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @Enumerated(EnumType.STRING)
  @Column(name = "event_type", nullable = false)
  private EventType eventType;

  @Column(name = "description")
  @NotBlank(message = "Event description is required")
  private String description;

  @Column(name = "previous_status")
  private String previousStatus;

  @Column(name = "new_status")
  private String newStatus;

  @Column(name = "metadata", columnDefinition = "TEXT")
  private String metadata; // JSON string for additional event data

  @Column(name = "user_id")
  private Long userId;

  @Column(name = "ip_address")
  private String ipAddress;

  @Column(name = "user_agent")
  private String userAgent;

  @CreationTimestamp
  @Column(name = "timestamp", nullable = false, updatable = false)
  private LocalDateTime timestamp;

  public enum EventType {
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
    TRACKING_UPDATED,
    SYSTEM_EVENT
  }
}
