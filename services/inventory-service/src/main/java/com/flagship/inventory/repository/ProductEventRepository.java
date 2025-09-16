package com.flagship.inventory.repository;

import com.flagship.inventory.model.ProductEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Product Event Repository
 * <p>
 * Data access layer for ProductEvent entities. Provides custom queries for product event tracking
 * and analytics.
 */
@Repository
public interface ProductEventRepository extends JpaRepository<ProductEvent, Long> {

  List<ProductEvent> findByProductIdOrderByTimestampDesc(Long productId);

  List<ProductEvent> findByProductIdAndEventTypeOrderByTimestampDesc(Long productId,
      ProductEvent.EventType eventType);

  List<ProductEvent> findByProductIdAndTimestampBetweenOrderByTimestampDesc(Long productId,
      LocalDateTime startDate, LocalDateTime endDate);

  List<ProductEvent> findByProductIdOrderByTimestampDesc(Long productId, Pageable pageable);

  List<ProductEvent> findByEventTypeOrderByTimestampDesc(ProductEvent.EventType eventType);

  List<ProductEvent> findByLocationOrderByTimestampDesc(String location);

  List<ProductEvent> findBySkuOrderByTimestampDesc(String sku);

  List<ProductEvent> findByOrderIdOrderByTimestampDesc(Long orderId);

  List<ProductEvent> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime startDate,
      LocalDateTime endDate);

  @Query("SELECT pe FROM ProductEvent pe WHERE pe.product.id = :productId ORDER BY pe.timestamp DESC")
  List<ProductEvent> findTopByProductIdOrderByTimestampDesc(@Param("productId") Long productId,
      @Param("limit") int limit);

  @Query("SELECT COUNT(pe) FROM ProductEvent pe WHERE pe.product.id = :productId AND pe.eventType = :eventType")
  long countByProductIdAndEventType(@Param("productId") Long productId,
      @Param("eventType") ProductEvent.EventType eventType);

  @Query("SELECT pe FROM ProductEvent pe WHERE pe.product.id = :productId AND pe.metadata IS NOT NULL ORDER BY pe.timestamp DESC")
  List<ProductEvent> findByProductIdWithMetadata(@Param("productId") Long productId);

  @Query("SELECT pe FROM ProductEvent pe WHERE pe.product.id = :productId AND LOWER(pe.description) LIKE LOWER(CONCAT('%', :descriptionPattern, '%')) ORDER BY pe.timestamp DESC")
  List<ProductEvent> findByProductIdAndDescriptionContaining(@Param("productId") Long productId,
      @Param("descriptionPattern") String descriptionPattern);

  @Query("SELECT pe FROM ProductEvent pe WHERE pe.product.id = :productId AND pe.location = :location ORDER BY pe.timestamp DESC")
  List<ProductEvent> findByProductIdAndLocation(@Param("productId") Long productId,
      @Param("location") String location);

  @Query("SELECT pe FROM ProductEvent pe WHERE pe.sku = :sku AND pe.eventType = :eventType ORDER BY pe.timestamp DESC")
  List<ProductEvent> findBySkuAndEventTypeOrderByTimestampDesc(@Param("sku") String sku,
      @Param("eventType") ProductEvent.EventType eventType);

  @Query("SELECT pe FROM ProductEvent pe WHERE pe.location = :location AND pe.eventType = :eventType ORDER BY pe.timestamp DESC")
  List<ProductEvent> findByLocationAndEventTypeOrderByTimestampDesc(
      @Param("location") String location, @Param("eventType") ProductEvent.EventType eventType);

  @Query("SELECT pe FROM ProductEvent pe WHERE pe.orderId = :orderId AND pe.eventType = :eventType ORDER BY pe.timestamp DESC")
  List<ProductEvent> findByOrderIdAndEventTypeOrderByTimestampDesc(@Param("orderId") Long orderId,
      @Param("eventType") ProductEvent.EventType eventType);

  @Query("SELECT pe FROM ProductEvent pe WHERE pe.sku = :sku AND pe.timestamp BETWEEN :startDate AND :endDate ORDER BY pe.timestamp DESC")
  List<ProductEvent> findBySkuAndTimestampBetweenOrderByTimestampDesc(@Param("sku") String sku,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query("SELECT pe FROM ProductEvent pe WHERE pe.location = :location AND pe.timestamp BETWEEN :startDate AND :endDate ORDER BY pe.timestamp DESC")
  List<ProductEvent> findByLocationAndTimestampBetweenOrderByTimestampDesc(
      @Param("location") String location,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query("SELECT pe FROM ProductEvent pe WHERE pe.eventType = :eventType AND pe.timestamp BETWEEN :startDate AND :endDate ORDER BY pe.timestamp DESC")
  List<ProductEvent> findByEventTypeAndTimestampBetweenOrderByTimestampDesc(
      @Param("eventType") ProductEvent.EventType eventType,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);
}
