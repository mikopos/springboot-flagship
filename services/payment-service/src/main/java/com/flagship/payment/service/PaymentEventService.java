package com.flagship.payment.service;

import com.flagship.payment.model.Payment;
import com.flagship.payment.model.PaymentEvent;
import com.flagship.payment.repository.PaymentEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Payment Event Service
 * <p>
 * Service for managing payment events and audit logs. Provides functionality to log and retrieve
 * payment events.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentEventService {

  private final PaymentEventRepository paymentEventRepository;

  public void logEvent(Payment payment, PaymentEvent.EventType eventType, String description,
      String previousStatus, String newStatus) {
    PaymentEvent event = PaymentEvent.builder()
        .payment(payment)
        .eventType(eventType)
        .description(description)
        .previousStatus(previousStatus)
        .newStatus(newStatus)
        .amount(payment.getAmount().toString())
        .currency(payment.getCurrency())
        .providerTransactionId(payment.getProviderTransactionId())
        .userId(payment.getUserId())
        .orderId(payment.getOrderId())
        .timestamp(LocalDateTime.now())
        .build();

    paymentEventRepository.save(event);
    log.debug("Logged payment event: {} for payment: {}", eventType, payment.getId());
  }

  public void logEvent(Payment payment, PaymentEvent.EventType eventType, String description,
      String previousStatus, String newStatus, String metadata) {
    PaymentEvent event = PaymentEvent.builder()
        .payment(payment)
        .eventType(eventType)
        .description(description)
        .previousStatus(previousStatus)
        .newStatus(newStatus)
        .amount(payment.getAmount().toString())
        .currency(payment.getCurrency())
        .providerTransactionId(payment.getProviderTransactionId())
        .metadata(metadata)
        .userId(payment.getUserId())
        .orderId(payment.getOrderId())
        .timestamp(LocalDateTime.now())
        .build();

    paymentEventRepository.save(event);
    log.debug("Logged payment event: {} for payment: {} with metadata", eventType, payment.getId());
  }

  public void logEvent(Payment payment, PaymentEvent.EventType eventType, String description,
      String previousStatus, String newStatus, String ipAddress, String userAgent) {
    PaymentEvent event = PaymentEvent.builder()
        .payment(payment)
        .eventType(eventType)
        .description(description)
        .previousStatus(previousStatus)
        .newStatus(newStatus)
        .amount(payment.getAmount().toString())
        .currency(payment.getCurrency())
        .providerTransactionId(payment.getProviderTransactionId())
        .ipAddress(ipAddress)
        .userAgent(userAgent)
        .userId(payment.getUserId())
        .orderId(payment.getOrderId())
        .timestamp(LocalDateTime.now())
        .build();

    paymentEventRepository.save(event);
    log.debug("Logged payment event: {} for payment: {} with IP and user agent", eventType,
        payment.getId());
  }

  @Transactional(readOnly = true)
  public List<PaymentEvent> getPaymentEvents(Long paymentId) {
    return paymentEventRepository.findByPaymentIdOrderByTimestampDesc(paymentId);
  }

  @Transactional(readOnly = true)
  public List<PaymentEvent> getPaymentEventsByType(Long paymentId,
      PaymentEvent.EventType eventType) {
    return paymentEventRepository.findByPaymentIdAndEventTypeOrderByTimestampDesc(paymentId,
        eventType);
  }

  @Transactional(readOnly = true)
  public List<PaymentEvent> getPaymentEventsByDateRange(Long paymentId, LocalDateTime startDate,
      LocalDateTime endDate) {
    return paymentEventRepository.findByPaymentIdAndTimestampBetweenOrderByTimestampDesc(paymentId,
        startDate, endDate);
  }

  @Transactional(readOnly = true)
  public List<PaymentEvent> getRecentPaymentEvents(Long paymentId, int limit) {
    return paymentEventRepository.findTopByPaymentIdOrderByTimestampDesc(paymentId, limit);
  }

  @Transactional(readOnly = true)
  public List<PaymentEvent> getPaymentEventsByEventType(PaymentEvent.EventType eventType) {
    return paymentEventRepository.findByEventTypeOrderByTimestampDesc(eventType);
  }

  @Transactional(readOnly = true)
  public List<PaymentEvent> getPaymentEventsByUserId(Long userId) {
    return paymentEventRepository.findByUserIdOrderByTimestampDesc(userId);
  }

  @Transactional(readOnly = true)
  public List<PaymentEvent> getPaymentEventsByOrderId(Long orderId) {
    return paymentEventRepository.findByOrderIdOrderByTimestampDesc(orderId);
  }

  @Transactional(readOnly = true)
  public List<PaymentEvent> getPaymentEventsByIpAddress(String ipAddress) {
    return paymentEventRepository.findByIpAddressOrderByTimestampDesc(ipAddress);
  }

  @Transactional(readOnly = true)
  public List<PaymentEvent> getPaymentEventsByDateRange(LocalDateTime startDate,
      LocalDateTime endDate) {
    return paymentEventRepository.findByTimestampBetweenOrderByTimestampDesc(startDate, endDate);
  }
}
