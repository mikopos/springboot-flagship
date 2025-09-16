package com.flagship.inventory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Inventory Item Entity
 * <p>
 * Represents an inventory item for a specific product and location. This entity manages stock
 * levels, reservations, and availability.
 */
@Entity
@Table(name = "inventory_items", indexes = {
    @Index(name = "idx_inventory_item_product_id", columnList = "product_id"),
    @Index(name = "idx_inventory_item_location", columnList = "location"),
    @Index(name = "idx_inventory_item_sku", columnList = "sku")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(name = "sku", nullable = false)
  @NotNull(message = "SKU is required")
  private String sku;

  @Column(name = "location", nullable = false)
  @NotNull(message = "Location is required")
  private String location;

  @Column(name = "quantity", nullable = false)
  @NotNull(message = "Quantity is required")
  @PositiveOrZero(message = "Quantity must be non-negative")
  @Builder.Default
  private Integer quantity = 0;

  @Column(name = "reserved_quantity", nullable = false)
  @NotNull(message = "Reserved quantity is required")
  @PositiveOrZero(message = "Reserved quantity must be non-negative")
  @Builder.Default
  private Integer reservedQuantity = 0;

  @Column(name = "available_quantity", nullable = false)
  @NotNull(message = "Available quantity is required")
  @PositiveOrZero(message = "Available quantity must be non-negative")
  @Builder.Default
  private Integer availableQuantity = 0;

  @Column(name = "reorder_point")
  @PositiveOrZero(message = "Reorder point must be non-negative")
  @Builder.Default
  private Integer reorderPoint = 0;

  @Column(name = "reorder_quantity")
  @PositiveOrZero(message = "Reorder quantity must be non-negative")
  @Builder.Default
  private Integer reorderQuantity = 0;

  @Column(name = "last_restocked")
  private LocalDateTime lastRestocked;

  @Column(name = "last_sold")
  private LocalDateTime lastSold;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  public void updateAvailableQuantity() {
    this.availableQuantity = Math.max(0, this.quantity - this.reservedQuantity);
  }

  public boolean reserveQuantity(Integer quantityToReserve) {
    if (quantityToReserve <= 0 || quantityToReserve > availableQuantity) {
      return false;
    }

    this.reservedQuantity += quantityToReserve;
    updateAvailableQuantity();
    return true;
  }

  public boolean releaseReservedQuantity(Integer quantityToRelease) {
    if (quantityToRelease <= 0 || quantityToRelease > reservedQuantity) {
      return false;
    }

    this.reservedQuantity -= quantityToRelease;
    updateAvailableQuantity();
    return true;
  }

  public boolean confirmReservedQuantity(Integer quantityToConfirm) {
    if (quantityToConfirm <= 0 || quantityToConfirm > reservedQuantity) {
      return false;
    }

    this.quantity -= quantityToConfirm;
    this.reservedQuantity -= quantityToConfirm;
    updateAvailableQuantity();
    this.lastSold = LocalDateTime.now();
    return true;
  }

  public void addQuantity(Integer quantityToAdd) {
    if (quantityToAdd > 0) {
      this.quantity += quantityToAdd;
      updateAvailableQuantity();
      this.lastRestocked = LocalDateTime.now();
    }
  }

  public boolean removeQuantity(Integer quantityToRemove) {
    if (quantityToRemove <= 0 || quantityToRemove > availableQuantity) {
      return false;
    }

    this.quantity -= quantityToRemove;
    updateAvailableQuantity();
    return true;
  }

  public boolean isInStock() {
    return availableQuantity > 0;
  }

  public boolean isLowStock() {
    return availableQuantity <= reorderPoint;
  }

  public boolean isOutOfStock() {
    return availableQuantity == 0;
  }

  public double getStockLevelPercentage() {
    if (reorderPoint == 0) {
      return 100.0;
    }
    return Math.min(100.0, (double) availableQuantity / reorderPoint * 100);
  }
}
