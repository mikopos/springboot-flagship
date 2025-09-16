package com.flagship.payment.repository;

import com.flagship.payment.model.PaymentEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Payment Event Repository
 * <p>
 * Data access layer for PaymentEvent entities. Provides custom queries for payment event tracking
 * and analytics.
 */
@Repository
public interface PaymentEventRepository extends JpaRepository<PaymentEvent, Long> {

  List<PaymentEvent> findByPaymentIdOrderByTimestampDesc(Long paymentId);

  List<PaymentEvent> findByPaymentIdAndEventTypeOrderByTimestampDesc(Long paymentId,
      PaymentEvent.EventType eventType);

  List<PaymentEvent> findByPaymentIdAndTimestampBetweenOrderByTimestampDesc(Long paymentId,
      LocalDateTime startDate, LocalDateTime endDate);

  List<PaymentEvent> findByPaymentIdOrderByTimestampDesc(Long paymentId, Pageable pageable);

  List<PaymentEvent> findByEventTypeOrderByTimestampDesc(PaymentEvent.EventType eventType);

  List<PaymentEvent> findByUserIdOrderByTimestampDesc(Long userId);

  List<PaymentEvent> findByOrderIdOrderByTimestampDesc(Long orderId);

  List<PaymentEvent> findByIpAddressOrderByTimestampDesc(String ipAddress);

  List<PaymentEvent> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime startDate,
      LocalDateTime endDate);

  @Query("SELECT pe FROM PaymentEvent pe WHERE pe.payment.id = :paymentId ORDER BY pe.timestamp DESC")
  List<PaymentEvent> findTopByPaymentIdOrderByTimestampDesc(@Param("paymentId") Long paymentId,
      @Param("limit") int limit);

  @Query("SELECT COUNT(pe) FROM PaymentEvent pe WHERE pe.payment.id = :paymentId AND pe.eventType = :eventType")
  long countByPaymentIdAndEventType(@Param("paymentId") Long paymentId,
      @Param("eventType") PaymentEvent.EventType eventType);

  @Query("SELECT pe FROM PaymentEvent pe WHERE pe.payment.id = :paymentId AND pe.metadata IS NOT NULL ORDER BY pe.timestamp DESC")
  List<PaymentEvent> findByPaymentIdWithMetadata(@Param("paymentId") Long paymentId);

  @Query("SELECT pe FROM PaymentEvent pe WHERE pe.payment.id = :paymentId AND LOWER(pe.description) LIKE LOWER(CONCAT('%', :descriptionPattern, '%')) ORDER BY pe.timestamp DESC")
  List<PaymentEvent> findByPaymentIdAndDescriptionContaining(@Param("paymentId") Long paymentId,
      @Param("descriptionPattern") String descriptionPattern);

  @Query("SELECT pe FROM PaymentEvent pe WHERE pe.payment.id = :paymentId AND pe.ipAddress = :ipAddress ORDER BY pe.timestamp DESC")
  List<PaymentEvent> findByPaymentIdAndIpAddress(@Param("paymentId") Long paymentId,
      @Param("ipAddress") String ipAddress);

  @Query("SELECT pe FROM PaymentEvent pe WHERE pe.userId = :userId AND pe.eventType = :eventType ORDER BY pe.timestamp DESC")
  List<PaymentEvent> findByUserIdAndEventTypeOrderByTimestampDesc(@Param("userId") Long userId,
      @Param("eventType") PaymentEvent.EventType eventType);

  @Query("SELECT pe FROM PaymentEvent pe WHERE pe.orderId = :orderId AND pe.eventType = :eventType ORDER BY pe.timestamp DESC")
  List<PaymentEvent> findByOrderIdAndEventTypeOrderByTimestampDesc(@Param("orderId") Long orderId,
      @Param("eventType") PaymentEvent.EventType eventType);

  @Query("SELECT pe FROM PaymentEvent pe WHERE pe.userId = :userId AND pe.timestamp BETWEEN :startDate AND :endDate ORDER BY pe.timestamp DESC")
  List<PaymentEvent> findByUserIdAndTimestampBetweenOrderByTimestampDesc(
      @Param("userId") Long userId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query("SELECT pe FROM PaymentEvent pe WHERE pe.orderId = :orderId AND pe.timestamp BETWEEN :startDate AND :endDate ORDER BY pe.timestamp DESC")
  List<PaymentEvent> findByOrderIdAndTimestampBetweenOrderByTimestampDesc(
      @Param("orderId") Long orderId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query("SELECT pe FROM PaymentEvent pe WHERE pe.eventType = :eventType AND pe.timestamp BETWEEN :startDate AND :endDate ORDER BY pe.timestamp DESC")
  List<PaymentEvent> findByEventTypeAndTimestampBetweenOrderByTimestampDesc(
      @Param("eventType") PaymentEvent.EventType eventType,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);
}
