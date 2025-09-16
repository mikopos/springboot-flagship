package com.flagship.inventory.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Inventory Event
 * <p>
 * Represents an inventory-related event that can be published to Kafka. Used for event-driven
 * communication between microservices.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryEvent {

  private Long productId;
  private String sku;
  private InventoryEventType eventType;
  private LocalDateTime timestamp;
  private String metadata;

  // Product details
  private String productName;
  private String category;
  private String brand;
  private BigDecimal price;
  private String currency;
  private String status;

  // Inventory details
  private Integer totalQuantity;
  private Integer availableQuantity;
  private Integer reservedQuantity;
  private String location;
  private Integer quantityChange;
  private Integer previousQuantity;
  private Integer newQuantity;

  // Order details
  private Long orderId;
  private Long userId;

  public enum InventoryEventType {
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
    STOCK_LEVEL_CHANGED
  }
}
