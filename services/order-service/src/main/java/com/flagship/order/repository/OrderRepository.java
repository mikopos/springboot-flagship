package com.flagship.order.repository;

import com.flagship.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Order Repository
 * <p>
 * Data access layer for Order entities. Provides custom queries for order management operations.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

  Optional<Order> findByOrderNumber(String orderNumber);

  List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

  List<Order> findByStatusOrderByCreatedAtDesc(Order.OrderStatus status);

  List<Order> findByPaymentStatusOrderByCreatedAtDesc(Order.PaymentStatus paymentStatus);

  List<Order> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, Order.OrderStatus status);

  List<Order> findByUserIdAndPaymentStatusOrderByCreatedAtDesc(Long userId,
      Order.PaymentStatus paymentStatus);

  List<Order> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime dateTime);

  List<Order> findByCreatedAtBeforeOrderByCreatedAtDesc(LocalDateTime dateTime);

  List<Order> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate,
      LocalDateTime endDate);

  List<Order> findByTotalAmountBetweenOrderByCreatedAtDesc(java.math.BigDecimal minAmount,
      java.math.BigDecimal maxAmount);

  List<Order> findByCurrencyOrderByCreatedAtDesc(String currency);

  List<Order> findByShippingMethodOrderByCreatedAtDesc(String shippingMethod);

  List<Order> findByPaymentMethodOrderByCreatedAtDesc(String paymentMethod);

  List<Order> findByTrackingNumberIsNotNullOrderByCreatedAtDesc();

  List<Order> findByTrackingNumberIsNullOrderByCreatedAtDesc();

  @Query("SELECT o FROM Order o WHERE o.userId = :userId ORDER BY o.createdAt DESC")
  List<Order> findTopByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId,
      @Param("limit") int limit);

  @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
  long countByStatus(@Param("status") Order.OrderStatus status);

  @Query("SELECT COUNT(o) FROM Order o WHERE o.paymentStatus = :paymentStatus")
  long countByPaymentStatus(@Param("paymentStatus") Order.PaymentStatus paymentStatus);

  @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.createdAt BETWEEN :startDate AND :endDate ORDER BY o.createdAt DESC")
  List<Order> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(@Param("userId") Long userId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query("SELECT o FROM Order o WHERE o.totalAmount BETWEEN :minAmount AND :maxAmount AND o.status = :status ORDER BY o.createdAt DESC")
  List<Order> findByTotalAmountBetweenAndStatusOrderByCreatedAtDesc(
      @Param("minAmount") java.math.BigDecimal minAmount,
      @Param("maxAmount") java.math.BigDecimal maxAmount,
      @Param("status") Order.OrderStatus status);

  @Query("SELECT o FROM Order o WHERE LOWER(o.shippingAddress) LIKE LOWER(CONCAT('%', :city, '%')) ORDER BY o.createdAt DESC")
  List<Order> findByShippingAddressContainingCityOrderByCreatedAtDesc(@Param("city") String city);

  @Query("SELECT o FROM Order o WHERE LOWER(o.shippingAddress) LIKE LOWER(CONCAT('%', :country, '%')) ORDER BY o.createdAt DESC")
  List<Order> findByShippingAddressContainingCountryOrderByCreatedAtDesc(
      @Param("country") String country);
}
