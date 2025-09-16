package com.flagship.payment.service;

import com.flagship.payment.event.PaymentEvent;
import com.flagship.payment.model.Payment;
import com.flagship.payment.repository.PaymentRepository;
import com.flagship.payment.client.PaymentProviderClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Payment Service
 * <p>
 * Business logic layer for payment management operations. Handles payment processing, idempotency,
 * and event publishing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;
  private final PaymentEventService paymentEventService;
  private final PaymentProviderClient paymentProviderClient;
  private final IdempotencyService idempotencyService;

  public Payment createPayment(Payment payment) {
    log.info("Creating new payment for order: {}", payment.getOrderId());

    payment.setPaymentId(generatePaymentId());

    payment.setStatus(Payment.PaymentStatus.PENDING);

    payment.setExpiresAt(LocalDateTime.now().plusHours(24));

    Payment savedPayment = paymentRepository.save(payment);

    paymentEventService.logEvent(savedPayment, PaymentEventEntity.EventType.PAYMENT_CREATED,
        "Payment created", null, null);

    publishPaymentEvent(savedPayment, PaymentEvent.PaymentEventType.PAYMENT_CREATED);

    log.info("Payment created successfully with ID: {} and payment ID: {}",
        savedPayment.getId(), savedPayment.getPaymentId());
    return savedPayment;
  }

  public Payment processPayment(String paymentId) {
    log.info("Processing payment: {}", paymentId);

    Payment payment = paymentRepository.findByPaymentId(paymentId)
        .orElseThrow(() -> new IllegalArgumentException("Payment not found with ID: " + paymentId));

    if (!payment.isPending()) {
      throw new IllegalStateException(
          "Payment cannot be processed in status: " + payment.getStatus());
    }

    if (payment.isExpired()) {
      payment.updateStatus(Payment.PaymentStatus.EXPIRED);
      paymentRepository.save(payment);
      throw new IllegalStateException("Payment has expired");
    }

    payment.updateStatus(Payment.PaymentStatus.PROCESSING);
    paymentRepository.save(payment);

    paymentEventService.logEvent(payment, PaymentEventEntity.EventType.PAYMENT_PROCESSING,
        "Payment processing started", null, null);

    try {
      PaymentProviderClient.PaymentResponse response = paymentProviderClient.processPayment(
          payment);

      payment.setProviderTransactionId(response.getTransactionId());
      payment.setProviderResponse(response.getResponse());

      if (response.isSuccess()) {
        payment.updateStatus(Payment.PaymentStatus.COMPLETED);
        log.info("Payment processed successfully: {}", paymentId);
      } else {
        payment.updateStatus(Payment.PaymentStatus.FAILED, response.getErrorMessage());
        log.error("Payment processing failed: {} - {}", paymentId, response.getErrorMessage());
      }

      Payment savedPayment = paymentRepository.save(payment);

      PaymentEventEntity.EventType eventType = response.isSuccess() ?
          PaymentEventEntity.EventType.PAYMENT_COMPLETED :
          PaymentEventEntity.EventType.PAYMENT_FAILED;
      paymentEventService.logEvent(savedPayment, eventType,
          response.isSuccess() ? "Payment completed successfully"
              : "Payment failed: " + response.getErrorMessage(),
          null, null);

      PaymentEvent.PaymentEventType publishEventType = response.isSuccess() ?
          PaymentEvent.PaymentEventType.PAYMENT_COMPLETED :
          PaymentEvent.PaymentEventType.PAYMENT_FAILED;
      publishPaymentEvent(savedPayment, publishEventType);

      return savedPayment;

    } catch (Exception e) {
      payment.updateStatus(Payment.PaymentStatus.FAILED, e.getMessage());
      Payment savedPayment = paymentRepository.save(payment);

      paymentEventService.logEvent(savedPayment, PaymentEventEntity.EventType.PAYMENT_FAILED,
          "Payment processing failed: " + e.getMessage(), null, null);

      publishPaymentEvent(savedPayment, PaymentEvent.PaymentEventType.PAYMENT_FAILED);

      log.error("Payment processing failed: {}", paymentId, e);
      throw new RuntimeException("Payment processing failed", e);
    }
  }

  public Payment processPaymentWithIdempotency(String paymentId, String idempotencyKey) {
    log.info("Processing payment with idempotency: {} - {}", paymentId, idempotencyKey);

    Optional<Payment> existingPayment = idempotencyService.getPaymentByIdempotencyKey(
        idempotencyKey);
    if (existingPayment.isPresent()) {
      log.info("Payment already processed with idempotency key: {}", idempotencyKey);
      return existingPayment.get();
    }

    Payment payment = processPayment(paymentId);

    idempotencyService.storePaymentIdempotency(idempotencyKey, payment);

    return payment;
  }

  @Transactional(readOnly = true)
  public Optional<Payment> findById(Long id) {
    return paymentRepository.findById(id);
  }

  @Transactional(readOnly = true)
  public Optional<Payment> findByPaymentId(String paymentId) {
    return paymentRepository.findByPaymentId(paymentId);
  }

  @Transactional(readOnly = true)
  public List<Payment> findByOrderId(Long orderId) {
    return paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
  }

  @Transactional(readOnly = true)
  public List<Payment> findByUserId(Long userId) {
    return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
  }

  public Payment refundPayment(String paymentId, BigDecimal refundAmount) {
    log.info("Refunding payment: {} with amount: {}", paymentId, refundAmount);

    Payment payment = paymentRepository.findByPaymentId(paymentId)
        .orElseThrow(() -> new IllegalArgumentException("Payment not found with ID: " + paymentId));

    if (!payment.canBeRefunded()) {
      throw new IllegalStateException(
          "Payment cannot be refunded in status: " + payment.getStatus());
    }

    if (refundAmount.compareTo(payment.getAmount()) > 0) {
      throw new IllegalArgumentException("Refund amount cannot exceed payment amount");
    }

    payment.updateRefundStatus(Payment.RefundStatus.PENDING, refundAmount);
    Payment savedPayment = paymentRepository.save(payment);

    paymentEventService.logEvent(savedPayment, PaymentEventEntity.EventType.REFUND_INITIATED,
        "Refund initiated for amount: " + refundAmount, null, null);

    try {
      PaymentProviderClient.RefundResponse response = paymentProviderClient.processRefund(payment,
          refundAmount);

      if (response.isSuccess()) {
        payment.updateRefundStatus(Payment.RefundStatus.COMPLETED, refundAmount);
        log.info("Refund processed successfully: {}", paymentId);
      } else {
        payment.updateRefundStatus(Payment.RefundStatus.FAILED, refundAmount);
        log.error("Refund processing failed: {} - {}", paymentId, response.getErrorMessage());
      }

      Payment finalPayment = paymentRepository.save(payment);

      PaymentEventEntity.EventType eventType = response.isSuccess() ?
          PaymentEventEntity.EventType.REFUND_COMPLETED :
          PaymentEventEntity.EventType.REFUND_FAILED;
      paymentEventService.logEvent(finalPayment, eventType,
          response.isSuccess() ? "Refund completed successfully"
              : "Refund failed: " + response.getErrorMessage(),
          null, null);

      PaymentEvent.PaymentEventType publishEventType = response.isSuccess() ?
          PaymentEvent.PaymentEventType.REFUND_COMPLETED :
          PaymentEvent.PaymentEventType.REFUND_FAILED;
      publishPaymentEvent(finalPayment, publishEventType);

      return finalPayment;

    } catch (Exception e) {
      payment.updateRefundStatus(Payment.RefundStatus.FAILED, refundAmount);
      Payment savedPayment = paymentRepository.save(payment);

      paymentEventService.logEvent(savedPayment, PaymentEventEntity.EventType.REFUND_FAILED,
          "Refund processing failed: " + e.getMessage(), null, null);

      publishPaymentEvent(savedPayment, PaymentEvent.PaymentEventType.REFUND_FAILED);

      log.error("Refund processing failed: {}", paymentId, e);
      throw new RuntimeException("Refund processing failed", e);
    }
  }

  public Payment cancelPayment(String paymentId) {
    log.info("Cancelling payment: {}", paymentId);

    Payment payment = paymentRepository.findByPaymentId(paymentId)
        .orElseThrow(() -> new IllegalArgumentException("Payment not found with ID: " + paymentId));

    if (!payment.isPending()) {
      throw new IllegalStateException(
          "Payment cannot be cancelled in status: " + payment.getStatus());
    }

    payment.updateStatus(Payment.PaymentStatus.CANCELLED);
    Payment savedPayment = paymentRepository.save(payment);

    paymentEventService.logEvent(savedPayment, PaymentEventEntity.EventType.PAYMENT_CANCELLED,
        "Payment cancelled", null, null);

    publishPaymentEvent(savedPayment, PaymentEvent.PaymentEventType.PAYMENT_CANCELLED);

    log.info("Payment cancelled successfully: {}", paymentId);
    return savedPayment;
  }

  @Transactional(readOnly = true)
  public List<Payment> findByStatus(Payment.PaymentStatus status) {
    return paymentRepository.findByStatusOrderByCreatedAtDesc(status);
  }

  @Transactional(readOnly = true)
  public List<Payment> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
    return paymentRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate);
  }

  private String generatePaymentId() {
    return "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
  }

  private void publishPaymentEvent(Payment payment, PaymentEvent.PaymentEventType eventType) {
    try {
      PaymentEvent event = PaymentEvent.builder()
          .paymentId(payment.getId())
          .paymentId(payment.getPaymentId())
          .orderId(payment.getOrderId())
          .userId(payment.getUserId())
          .eventType(eventType)
          .timestamp(LocalDateTime.now())
          .amount(payment.getAmount())
          .currency(payment.getCurrency())
          .status(payment.getStatus().toString())
          .paymentMethod(payment.getPaymentMethod().toString())
          .providerTransactionId(payment.getProviderTransactionId())
          .build();

      kafkaTemplate.send("payment-events", event);
      log.debug("Published payment event: {} for payment: {}", eventType, payment.getId());
    } catch (Exception e) {
      log.error("Failed to publish payment event: {} for payment: {}", eventType, payment.getId(),
          e);
    }
  }
}
