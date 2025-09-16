package com.flagship.payment.model;

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
 * Payment Entity
 * <p>
 * Represents a payment transaction in the system. This entity manages the complete payment
 * lifecycle from initiation to completion.
 */
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payment_order_id", columnList = "orderId"),
    @Index(name = "idx_payment_user_id", columnList = "userId"),
    @Index(name = "idx_payment_status", columnList = "status"),
    @Index(name = "idx_payment_created_at", columnList = "createdAt"),
    @Index(name = "idx_payment_idempotency_key", columnList = "idempotencyKey")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "payment_id", unique = true, nullable = false)
  @NotBlank(message = "Payment ID is required")
  private String paymentId;

  @Column(name = "order_id", nullable = false)
  @NotNull(message = "Order ID is required")
  private Long orderId;

  @Column(name = "user_id", nullable = false)
  @NotNull(message = "User ID is required")
  private Long userId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  @Builder.Default
  private PaymentStatus status = PaymentStatus.PENDING;

  @Column(name = "amount", nullable = false, precision = 10, scale = 2)
  @NotNull(message = "Amount is required")
  @Positive(message = "Amount must be positive")
  private BigDecimal amount;

  @Column(name = "currency", nullable = false)
  @NotBlank(message = "Currency is required")
  @Builder.Default
  private String currency = "USD";

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_method", nullable = false)
  @NotNull(message = "Payment method is required")
  private PaymentMethod paymentMethod;

  @Column(name = "payment_provider")
  private String paymentProvider;

  @Column(name = "provider_transaction_id")
  private String providerTransactionId;

  @Column(name = "provider_response", columnDefinition = "TEXT")
  private String providerResponse;

  @Column(name = "idempotency_key", unique = true, nullable = false)
  @NotBlank(message = "Idempotency key is required")
  private String idempotencyKey;

  @Column(name = "description")
  private String description;

  @Column(name = "failure_reason")
  private String failureReason;

  @Column(name = "processed_at")
  private LocalDateTime processedAt;

  @Column(name = "expires_at")
  private LocalDateTime expiresAt;

  @Column(name = "refund_amount", precision = 10, scale = 2)
  @Builder.Default
  private BigDecimal refundAmount = BigDecimal.ZERO;

  @Enumerated(EnumType.STRING)
  @Column(name = "refund_status")
  @Builder.Default
  private RefundStatus refundStatus = RefundStatus.NONE;

  @Column(name = "metadata", columnDefinition = "TEXT")
  private String metadata; // JSON string for additional payment data

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Builder.Default
  private List<PaymentEvent> events = new ArrayList<>();

  public void updateStatus(PaymentStatus newStatus) {
    this.status = newStatus;
    this.updatedAt = LocalDateTime.now();

    if (newStatus == PaymentStatus.COMPLETED || newStatus == PaymentStatus.FAILED) {
      this.processedAt = LocalDateTime.now();
    }
  }

  public void updateStatus(PaymentStatus newStatus, String failureReason) {
    updateStatus(newStatus);
    this.failureReason = failureReason;
  }

  public void updateRefundStatus(RefundStatus newStatus, BigDecimal refundAmount) {
    this.refundStatus = newStatus;
    this.refundAmount = refundAmount;
    this.updatedAt = LocalDateTime.now();
  }

  public boolean isCompleted() {
    return status == PaymentStatus.COMPLETED;
  }

  public boolean isFailed() {
    return status == PaymentStatus.FAILED;
  }

  public boolean isPending() {
    return status == PaymentStatus.PENDING;
  }

  public boolean isExpired() {
    return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
  }

  public boolean canBeRefunded() {
    return isCompleted() && refundStatus == RefundStatus.NONE;
  }

  public boolean isFullyRefunded() {
    return refundStatus == RefundStatus.COMPLETED &&
        refundAmount.compareTo(amount) >= 0;
  }


  public enum PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED,
    EXPIRED
  }

  public enum PaymentMethod {
    CREDIT_CARD,
    DEBIT_CARD,
    PAYPAL,
    BANK_TRANSFER,
    DIGITAL_WALLET,
    CRYPTOCURRENCY
  }

  public enum RefundStatus {
    NONE,
    PENDING,
    COMPLETED,
    FAILED
  }
}
