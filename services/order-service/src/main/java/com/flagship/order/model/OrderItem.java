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

/**
 * Order Item Entity
 * <p>
 * Represents an item within an order. This entity stores information about individual products in
 * an order.
 */
@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order_item_order_id", columnList = "order_id"),
    @Index(name = "idx_order_item_product_id", columnList = "productId")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @Column(name = "product_id", nullable = false)
  @NotNull(message = "Product ID is required")
  private Long productId;

  @Column(name = "product_name", nullable = false)
  @NotBlank(message = "Product name is required")
  private String productName;

  @Column(name = "product_sku")
  private String productSku;

  @Column(name = "quantity", nullable = false)
  @NotNull(message = "Quantity is required")
  @Positive(message = "Quantity must be positive")
  private Integer quantity;

  @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
  @NotNull(message = "Unit price is required")
  @Positive(message = "Unit price must be positive")
  private BigDecimal unitPrice;

  @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
  @NotNull(message = "Total price is required")
  @Positive(message = "Total price must be positive")
  private BigDecimal totalPrice;

  @Column(name = "discount_amount", precision = 10, scale = 2)
  @Builder.Default
  private BigDecimal discountAmount = BigDecimal.ZERO;

  @Column(name = "tax_amount", precision = 10, scale = 2)
  @Builder.Default
  private BigDecimal taxAmount = BigDecimal.ZERO;

  @Column(name = "product_image_url")
  private String productImageUrl;

  @Column(name = "product_description", columnDefinition = "TEXT")
  private String productDescription;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  public BigDecimal calculateTotalPrice() {
    BigDecimal baseTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    BigDecimal afterDiscount = baseTotal.subtract(discountAmount);
    return afterDiscount.add(taxAmount);
  }

  public void updateTotalPrice() {
    this.totalPrice = calculateTotalPrice();
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
    updateTotalPrice();
  }

  public void setUnitPrice(BigDecimal unitPrice) {
    this.unitPrice = unitPrice;
    updateTotalPrice();
  }

  public void setDiscountAmount(BigDecimal discountAmount) {
    this.discountAmount = discountAmount;
    updateTotalPrice();
  }

  public void setTaxAmount(BigDecimal taxAmount) {
    this.taxAmount = taxAmount;
    updateTotalPrice();
  }
}
