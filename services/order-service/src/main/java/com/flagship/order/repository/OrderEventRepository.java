package com.flagship.order.repository;

import com.flagship.order.model.OrderEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Order Event Repository
 * <p>
 * Data access layer for OrderEvent entities. Provides custom queries for order event tracking and
 * analytics.
 */
@Repository
public interface OrderEventRepository extends JpaRepository<OrderEvent, Long> {

  List<OrderEvent> findByOrderIdOrderByTimestampDesc(Long orderId);

  List<OrderEvent> findByOrderIdAndEventTypeOrderByTimestampDesc(Long orderId,
      OrderEvent.EventType eventType);

  List<OrderEvent> findByOrderIdAndTimestampBetweenOrderByTimestampDesc(Long orderId,
      LocalDateTime startDate, LocalDateTime endDate);

  List<OrderEvent> findByOrderIdOrderByTimestampDesc(Long orderId, Pageable pageable);

  List<OrderEvent> findByEventTypeOrderByTimestampDesc(OrderEvent.EventType eventType);

  List<OrderEvent> findByUserIdOrderByTimestampDesc(Long userId);

  List<OrderEvent> findByIpAddressOrderByTimestampDesc(String ipAddress);

  List<OrderEvent> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime startDate,
      LocalDateTime endDate);

  @Query("SELECT oe FROM OrderEvent oe WHERE oe.order.id = :orderId ORDER BY oe.timestamp DESC")
  List<OrderEvent> findTopByOrderIdOrderByTimestampDesc(@Param("orderId") Long orderId,
      @Param("limit") int limit);

  @Query("SELECT COUNT(oe) FROM OrderEvent oe WHERE oe.order.id = :orderId AND oe.eventType = :eventType")
  long countByOrderIdAndEventType(@Param("orderId") Long orderId,
      @Param("eventType") OrderEvent.EventType eventType);

  @Query("SELECT oe FROM OrderEvent oe WHERE oe.order.id = :orderId AND oe.metadata IS NOT NULL ORDER BY oe.timestamp DESC")
  List<OrderEvent> findByOrderIdWithMetadata(@Param("orderId") Long orderId);

  @Query("SELECT oe FROM OrderEvent oe WHERE oe.order.id = :orderId AND LOWER(oe.description) LIKE LOWER(CONCAT('%', :descriptionPattern, '%')) ORDER BY oe.timestamp DESC")
  List<OrderEvent> findByOrderIdAndDescriptionContaining(@Param("orderId") Long orderId,
      @Param("descriptionPattern") String descriptionPattern);

  @Query("SELECT oe FROM OrderEvent oe WHERE oe.order.id = :orderId AND oe.ipAddress = :ipAddress ORDER BY oe.timestamp DESC")
  List<OrderEvent> findByOrderIdAndIpAddress(@Param("orderId") Long orderId,
      @Param("ipAddress") String ipAddress);

  @Query("SELECT oe FROM OrderEvent oe WHERE oe.userId = :userId AND oe.eventType = :eventType ORDER BY oe.timestamp DESC")
  List<OrderEvent> findByUserIdAndEventTypeOrderByTimestampDesc(@Param("userId") Long userId,
      @Param("eventType") OrderEvent.EventType eventType);

  @Query("SELECT oe FROM OrderEvent oe WHERE oe.userId = :userId AND oe.timestamp BETWEEN :startDate AND :endDate ORDER BY oe.timestamp DESC")
  List<OrderEvent> findByUserIdAndTimestampBetweenOrderByTimestampDesc(@Param("userId") Long userId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query("SELECT oe FROM OrderEvent oe WHERE oe.eventType = :eventType AND oe.timestamp BETWEEN :startDate AND :endDate ORDER BY oe.timestamp DESC")
  List<OrderEvent> findByEventTypeAndTimestampBetweenOrderByTimestampDesc(
      @Param("eventType") OrderEvent.EventType eventType,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);
}
