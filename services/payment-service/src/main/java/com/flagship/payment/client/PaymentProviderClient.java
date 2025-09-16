package com.flagship.payment.client;

import com.flagship.payment.model.Payment;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Payment Provider Client
 * <p>
 * Client for integrating with external payment providers. Implements resilience patterns including
 * circuit breaker, retry, and timeout.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentProviderClient {

  private final RestTemplate restTemplate;
  private static final String PAYMENT_PROVIDER_URL = "https://api.paymentprovider.com";

  @CircuitBreaker(name = "payment-provider", fallbackMethod = "processPaymentFallback")
  @Retry(name = "payment-provider")
  @TimeLimiter(name = "payment-provider")
  public CompletableFuture<PaymentResponse> processPaymentAsync(Payment payment) {
    return CompletableFuture.supplyAsync(() -> {
      log.info("Processing payment with external provider: {}", payment.getPaymentId());

      // Simulate external API call
      try {
        Thread.sleep(1000); // Simulate network delay

        // Simulate success/failure based on amount
        boolean success = payment.getAmount().compareTo(new BigDecimal("1000")) <= 0;

        if (success) {
          return PaymentResponse.builder()
              .success(true)
              .transactionId("TXN-" + System.currentTimeMillis())
              .response("Payment processed successfully")
              .build();
        } else {
          return PaymentResponse.builder()
              .success(false)
              .errorMessage("Payment amount exceeds limit")
              .build();
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("Payment processing interrupted", e);
      }
    });
  }

  @CircuitBreaker(name = "payment-provider", fallbackMethod = "processPaymentFallback")
  @Retry(name = "payment-provider")
  public PaymentResponse processPayment(Payment payment) {
    log.info("Processing payment with external provider: {}", payment.getPaymentId());

    // Simulate external API call
    try {
      Thread.sleep(1000); // Simulate network delay

      // Simulate success/failure based on amount
      boolean success = payment.getAmount().compareTo(new BigDecimal("1000")) <= 0;

      if (success) {
        return PaymentResponse.builder()
            .success(true)
            .transactionId("TXN-" + System.currentTimeMillis())
            .response("Payment processed successfully")
            .build();
      } else {
        return PaymentResponse.builder()
            .success(false)
            .errorMessage("Payment amount exceeds limit")
            .build();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Payment processing interrupted", e);
    }
  }

  @CircuitBreaker(name = "payment-provider", fallbackMethod = "processRefundFallback")
  @Retry(name = "payment-provider")
  public RefundResponse processRefund(Payment payment, BigDecimal refundAmount) {
    log.info("Processing refund with external provider: {} - {}", payment.getPaymentId(),
        refundAmount);

    // Simulate external API call
    try {
      Thread.sleep(1000); // Simulate network delay

      // Simulate success/failure based on refund amount
      boolean success = refundAmount.compareTo(payment.getAmount()) <= 0;

      if (success) {
        return RefundResponse.builder()
            .success(true)
            .refundId("REF-" + System.currentTimeMillis())
            .response("Refund processed successfully")
            .build();
      } else {
        return RefundResponse.builder()
            .success(false)
            .errorMessage("Refund amount exceeds payment amount")
            .build();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Refund processing interrupted", e);
    }
  }

  public PaymentResponse processPaymentFallback(Payment payment, Exception ex) {
    log.error("Payment processing fallback triggered for payment: {}", payment.getPaymentId(), ex);

    return PaymentResponse.builder()
        .success(false)
        .errorMessage("Payment provider unavailable: " + ex.getMessage())
        .build();
  }

  public RefundResponse processRefundFallback(Payment payment, BigDecimal refundAmount,
      Exception ex) {
    log.error("Refund processing fallback triggered for payment: {}", payment.getPaymentId(), ex);

    return RefundResponse.builder()
        .success(false)
        .errorMessage("Payment provider unavailable: " + ex.getMessage())
        .build();
  }


  @lombok.Data
  @lombok.Builder
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  public static class PaymentResponse {

    private boolean success;
    private String transactionId;
    private String response;
    private String errorMessage;
    private LocalDateTime timestamp;
  }

  @lombok.Data
  @lombok.Builder
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  public static class RefundResponse {

    private boolean success;
    private String refundId;
    private String response;
    private String errorMessage;
    private LocalDateTime timestamp;
  }
}
