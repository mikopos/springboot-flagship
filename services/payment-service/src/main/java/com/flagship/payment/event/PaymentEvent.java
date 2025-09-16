package com.flagship.payment.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Event
 * <p>
 * Represents a payment-related event that can be published to Kafka. Used for event-driven
 * communication between microservices.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {

  private Long id;
  private String paymentId;
  private Long orderId;
  private Long userId;
  private PaymentEventType eventType;
  private LocalDateTime timestamp;
  private String metadata;

  private BigDecimal amount;
  private String currency;
  private String status;
  private String paymentMethod;
  private String paymentProvider;
  private String providerTransactionId;
  private String idempotencyKey;
  private String failureReason;
  private LocalDateTime processedAt;
  private LocalDateTime expiresAt;

  private BigDecimal refundAmount;
  private String refundStatus;

  public enum PaymentEventType {
    PAYMENT_CREATED,
    PAYMENT_INITIATED,
    PAYMENT_PROCESSING,
    PAYMENT_COMPLETED,
    PAYMENT_FAILED,
    PAYMENT_CANCELLED,
    PAYMENT_EXPIRED,
    REFUND_INITIATED,
    REFUND_COMPLETED,
    REFUND_FAILED,
    CHARGEBACK_INITIATED,
    CHARGEBACK_RESOLVED,
    FRAUD_DETECTED,
    FRAUD_RESOLVED,
    STATUS_CHANGED,
    PROVIDER_RESPONSE
  }
}
