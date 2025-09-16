package com.flagship.inventory.model;

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
 * Product Entity
 * <p>
 * Represents a product in the inventory system. This entity manages product information and
 * inventory levels.
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_sku", columnList = "sku"),
    @Index(name = "idx_product_name", columnList = "name"),
    @Index(name = "idx_product_category", columnList = "category"),
    @Index(name = "idx_product_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "sku", unique = true, nullable = false)
  @NotBlank(message = "SKU is required")
  private String sku;

  @Column(name = "name", nullable = false)
  @NotBlank(message = "Product name is required")
  private String name;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "category")
  private String category;

  @Column(name = "brand")
  private String brand;

  @Column(name = "price", nullable = false, precision = 10, scale = 2)
  @NotNull(message = "Price is required")
  @Positive(message = "Price must be positive")
  private BigDecimal price;

  @Column(name = "currency", nullable = false)
  @NotBlank(message = "Currency is required")
  @Builder.Default
  private String currency = "USD";

  @Column(name = "weight")
  private BigDecimal weight;

  @Column(name = "dimensions")
  private String dimensions;

  @Column(name = "image_url")
  private String imageUrl;

  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private ProductStatus status = ProductStatus.ACTIVE;

  @Column(name = "is_digital")
  @Builder.Default
  private Boolean isDigital = false;

  @Column(name = "requires_shipping")
  @Builder.Default
  private Boolean requiresShipping = true;

  @Column(name = "tax_category")
  private String taxCategory;

  @Column(name = "metadata", columnDefinition = "TEXT")
  private String metadata; // JSON string for additional product data

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Builder.Default
  private List<InventoryItem> inventoryItems = new ArrayList<>();

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Builder.Default
  private List<ProductEvent> events = new ArrayList<>();

  public Integer getTotalAvailableQuantity() {
    return inventoryItems.stream()
        .mapToInt(InventoryItem::getAvailableQuantity)
        .sum();
  }

  public Integer getTotalReservedQuantity() {
    return inventoryItems.stream()
        .mapToInt(InventoryItem::getReservedQuantity)
        .sum();
  }

  public Integer getTotalQuantity() {
    return inventoryItems.stream()
        .mapToInt(InventoryItem::getQuantity)
        .sum();
  }

  public boolean isInStock() {
    return getTotalAvailableQuantity() > 0;
  }

  public boolean isLowStock(int threshold) {
    return getTotalAvailableQuantity() <= threshold;
  }

  public boolean isOutOfStock() {
    return getTotalAvailableQuantity() == 0;
  }

  public boolean isActive() {
    return status == ProductStatus.ACTIVE;
  }

  public void activate() {
    this.status = ProductStatus.ACTIVE;
  }

  public void deactivate() {
    this.status = ProductStatus.INACTIVE;
  }

  public enum ProductStatus {
    ACTIVE,
    INACTIVE,
    DISCONTINUED,
    OUT_OF_STOCK
  }
}
