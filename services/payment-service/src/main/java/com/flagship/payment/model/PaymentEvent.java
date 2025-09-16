package com.flagship.payment.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Payment Event Entity
 * <p>
 * Tracks payment events and state changes for audit and analytics purposes. This entity stores
 * information about payment lifecycle events.
 */
@Entity
@Table(name = "payment_events", indexes = {
    @Index(name = "idx_payment_event_payment_id", columnList = "payment_id"),
    @Index(name = "idx_payment_event_type", columnList = "event_type"),
    @Index(name = "idx_payment_event_timestamp", columnList = "timestamp")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "payment_id", nullable = false)
  private Payment payment;

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

  @Column(name = "amount")
  private String amount;

  @Column(name = "currency")
  private String currency;

  @Column(name = "provider_transaction_id")
  private String providerTransactionId;

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
    PROVIDER_RESPONSE,
    SYSTEM_EVENT
  }
}
