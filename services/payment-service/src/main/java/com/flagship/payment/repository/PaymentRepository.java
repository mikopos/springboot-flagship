package com.flagship.payment.repository;

import com.flagship.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Payment Repository
 * <p>
 * Data access layer for Payment entities. Provides custom queries for payment management
 * operations.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

  Optional<Payment> findByPaymentId(String paymentId);

  List<Payment> findByOrderIdOrderByCreatedAtDesc(Long orderId);

  List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);

  List<Payment> findByStatusOrderByCreatedAtDesc(Payment.PaymentStatus status);

  List<Payment> findByPaymentMethodOrderByCreatedAtDesc(Payment.PaymentMethod paymentMethod);

  List<Payment> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId,
      Payment.PaymentStatus status);

  List<Payment> findByOrderIdAndStatusOrderByCreatedAtDesc(Long orderId,
      Payment.PaymentStatus status);

  List<Payment> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime dateTime);

  List<Payment> findByCreatedAtBeforeOrderByCreatedAtDesc(LocalDateTime dateTime);

  List<Payment> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate,
      LocalDateTime endDate);

  List<Payment> findByAmountBetweenOrderByCreatedAtDesc(java.math.BigDecimal minAmount,
      java.math.BigDecimal maxAmount);

  List<Payment> findByCurrencyOrderByCreatedAtDesc(String currency);

  List<Payment> findByPaymentProviderOrderByCreatedAtDesc(String paymentProvider);

  List<Payment> findByProviderTransactionIdOrderByCreatedAtDesc(String providerTransactionId);

  List<Payment> findByIdempotencyKeyOrderByCreatedAtDesc(String idempotencyKey);

  List<Payment> findByRefundStatusOrderByCreatedAtDesc(Payment.RefundStatus refundStatus);

  List<Payment> findByExpiresAtBeforeAndStatusOrderByCreatedAtDesc(LocalDateTime currentTime,
      Payment.PaymentStatus status);

  @Query(value = "SELECT * FROM payments p WHERE p.user_id = :userId ORDER BY p.created_at DESC LIMIT :limit", nativeQuery = true)
  List<Payment> findTopByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId,
      @Param("limit") int limit);

  @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
  long countByStatus(@Param("status") Payment.PaymentStatus status);

  @Query("SELECT COUNT(p) FROM Payment p WHERE p.paymentMethod = :paymentMethod")
  long countByPaymentMethod(@Param("paymentMethod") Payment.PaymentMethod paymentMethod);

  @Query("SELECT p FROM Payment p WHERE p.userId = :userId AND p.createdAt BETWEEN :startDate AND :endDate ORDER BY p.createdAt DESC")
  List<Payment> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(@Param("userId") Long userId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query("SELECT p FROM Payment p WHERE p.amount BETWEEN :minAmount AND :maxAmount AND p.status = :status ORDER BY p.createdAt DESC")
  List<Payment> findByAmountBetweenAndStatusOrderByCreatedAtDesc(
      @Param("minAmount") java.math.BigDecimal minAmount,
      @Param("maxAmount") java.math.BigDecimal maxAmount,
      @Param("status") Payment.PaymentStatus status);

  @Query("SELECT p FROM Payment p WHERE p.orderId = :orderId AND p.createdAt BETWEEN :startDate AND :endDate ORDER BY p.createdAt DESC")
  List<Payment> findByOrderIdAndCreatedAtBetweenOrderByCreatedAtDesc(@Param("orderId") Long orderId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query("SELECT p FROM Payment p WHERE p.paymentProvider = :paymentProvider AND p.status = :status ORDER BY p.createdAt DESC")
  List<Payment> findByPaymentProviderAndStatusOrderByCreatedAtDesc(
      @Param("paymentProvider") String paymentProvider,
      @Param("status") Payment.PaymentStatus status);
}
