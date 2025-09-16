package com.flagship.inventory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Product Event Entity
 * <p>
 * Tracks product and inventory events for audit and analytics purposes. This entity stores
 * information about inventory changes and product events.
 */
@Entity
@Table(name = "product_events", indexes = {
    @Index(name = "idx_product_event_product_id", columnList = "product_id"),
    @Index(name = "idx_product_event_type", columnList = "event_type"),
    @Index(name = "idx_product_event_timestamp", columnList = "timestamp")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Enumerated(EnumType.STRING)
  @Column(name = "event_type", nullable = false)
  private EventType eventType;

  @Column(name = "description")
  @NotBlank(message = "Event description is required")
  private String description;

  @Column(name = "previous_quantity")
  private Integer previousQuantity;

  @Column(name = "new_quantity")
  private Integer newQuantity;

  @Column(name = "quantity_change")
  private Integer quantityChange;

  @Column(name = "location")
  private String location;

  @Column(name = "sku")
  private String sku;

  @Column(name = "metadata", columnDefinition = "TEXT")
  private String metadata; // JSON string for additional event data

  @Column(name = "user_id")
  private Long userId;

  @Column(name = "order_id")
  private Long orderId;

  @Column(name = "ip_address")
  private String ipAddress;

  @Column(name = "user_agent")
  private String userAgent;

  @CreationTimestamp
  @Column(name = "timestamp", nullable = false, updatable = false)
  private LocalDateTime timestamp;

  public enum EventType {
    PRODUCT_CREATED,
    PRODUCT_UPDATED,
    PRODUCT_DEACTIVATED,
    PRODUCT_ACTIVATED,
    INVENTORY_ADDED,
    INVENTORY_REMOVED,
    INVENTORY_RESERVED,
    INVENTORY_RELEASED,
    INVENTORY_CONFIRMED,
    INVENTORY_ADJUSTED,
    LOW_STOCK_ALERT,
    OUT_OF_STOCK_ALERT,
    REORDER_POINT_REACHED,
    STOCK_LEVEL_CHANGED,
    SYSTEM_EVENT
  }
}
