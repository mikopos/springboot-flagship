package com.flagship.payment.controller;

import com.flagship.payment.model.Payment;
import com.flagship.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Payment Controller
 * <p>
 * REST API endpoints for payment management operations. Provides CRUD operations for payments and
 * payment processing.
 */
@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentService paymentService;

  @PostMapping
  public ResponseEntity<Payment> createPayment(@Valid @RequestBody Payment payment) {
    log.info("Creating new payment for order: {}", payment.getOrderId());
    Payment createdPayment = paymentService.createPayment(payment);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdPayment);
  }

  @PostMapping("/{paymentId}/process")
  @PreAuthorize("hasRole('ADMIN') or hasRole('PAYMENT_MANAGER') or @paymentService.isPaymentOwner(#paymentId, authentication)")
  public ResponseEntity<Payment> processPayment(@PathVariable String paymentId) {
    log.info("Processing payment: {}", paymentId);

    try {
      Payment processedPayment = paymentService.processPayment(paymentId);
      return ResponseEntity.ok(processedPayment);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    } catch (IllegalStateException e) {
      return ResponseEntity.badRequest().build();
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PostMapping("/{paymentId}/idempotentprocess")
  @PreAuthorize("hasRole('ADMIN') or hasRole('PAYMENT_MANAGER') or @paymentService.isPaymentOwner(#paymentId, authentication)")
  public ResponseEntity<Payment> processPaymentWithIdempotency(@PathVariable String paymentId,
      @RequestParam String idempotencyKey) {
    log.info("Processing payment with idempotency: {} - {}", paymentId, idempotencyKey);

    try {
      Payment processedPayment = paymentService.processPaymentWithIdempotency(paymentId,
          idempotencyKey);
      return ResponseEntity.ok(processedPayment);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    } catch (IllegalStateException e) {
      return ResponseEntity.badRequest().build();
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('PAYMENT_MANAGER') or @paymentService.isPaymentOwner(#id, authentication)")
  public ResponseEntity<Payment> getPaymentById(@PathVariable Long id,
      @AuthenticationPrincipal Jwt jwt) {
    log.debug("Getting payment by ID: {}", id);

    Optional<Payment> payment = paymentService.findById(id);
    return payment.map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/payment-id/{paymentId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('PAYMENT_MANAGER') or @paymentService.isPaymentOwnerByPaymentId(#paymentId, authentication)")
  public ResponseEntity<Payment> getPaymentByPaymentId(@PathVariable String paymentId,
      @AuthenticationPrincipal Jwt jwt) {
    log.debug("Getting payment by payment ID: {}", paymentId);

    Optional<Payment> payment = paymentService.findByPaymentId(paymentId);
    return payment.map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/my-payments")
  public ResponseEntity<List<Payment>> getMyPayments(@AuthenticationPrincipal Jwt jwt) {
    String userId = jwt.getClaim("sub");
    log.debug("Getting payments for user: {}", userId);

    List<Payment> payments = paymentService.findByUserId(Long.valueOf(userId));
    return ResponseEntity.ok(payments);
  }

  @GetMapping("/order/{orderId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('PAYMENT_MANAGER') or @paymentService.isOrderOwner(#orderId, authentication)")
  public ResponseEntity<List<Payment>> getPaymentsByOrderId(@PathVariable Long orderId) {
    log.debug("Getting payments for order: {}", orderId);

    List<Payment> payments = paymentService.findByOrderId(orderId);
    return ResponseEntity.ok(payments);
  }

  @PostMapping("/{paymentId}/refund")
  @PreAuthorize("hasRole('ADMIN') or hasRole('PAYMENT_MANAGER')")
  public ResponseEntity<Payment> refundPayment(@PathVariable String paymentId,
      @RequestParam BigDecimal refundAmount) {
    log.info("Refunding payment: {} with amount: {}", paymentId, refundAmount);

    try {
      Payment refundedPayment = paymentService.refundPayment(paymentId, refundAmount);
      return ResponseEntity.ok(refundedPayment);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    } catch (IllegalStateException e) {
      return ResponseEntity.badRequest().build();
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PutMapping("/{paymentId}/cancel")
  @PreAuthorize("hasRole('ADMIN') or hasRole('PAYMENT_MANAGER') or @paymentService.isPaymentOwner(#paymentId, authentication)")
  public ResponseEntity<Payment> cancelPayment(@PathVariable String paymentId) {
    log.info("Cancelling payment: {}", paymentId);

    try {
      Payment cancelledPayment = paymentService.cancelPayment(paymentId);
      return ResponseEntity.ok(cancelledPayment);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    } catch (IllegalStateException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('PAYMENT_MANAGER')")
  public ResponseEntity<Page<Payment>> getAllPayments(Pageable pageable) {
    log.debug("Getting all payments with pagination: {}", pageable);

    // Note: This would need to be implemented in PaymentService with pagination support
    List<Payment> payments = paymentService.findByStatus(Payment.PaymentStatus.PENDING);
    // For now, return payments without pagination
    return ResponseEntity.ok(Page.empty());
  }

  @GetMapping("/status/{status}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('PAYMENT_MANAGER')")
  public ResponseEntity<List<Payment>> getPaymentsByStatus(
      @PathVariable Payment.PaymentStatus status) {
    log.debug("Getting payments by status: {}", status);

    List<Payment> payments = paymentService.findByStatus(status);
    return ResponseEntity.ok(payments);
  }

  @GetMapping("/date-range")
  @PreAuthorize("hasRole('ADMIN') or hasRole('PAYMENT_MANAGER')")
  public ResponseEntity<List<Payment>> getPaymentsByDateRange(@RequestParam LocalDateTime startDate,
      @RequestParam LocalDateTime endDate) {
    log.debug("Getting payments by date range: {} to {}", startDate, endDate);

    List<Payment> payments = paymentService.findByDateRange(startDate, endDate);
    return ResponseEntity.ok(payments);
  }

  @GetMapping("/health")
  public ResponseEntity<String> health() {
    return ResponseEntity.ok("Payment service is healthy");
  }
}
