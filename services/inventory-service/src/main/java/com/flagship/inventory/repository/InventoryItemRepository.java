package com.flagship.inventory.repository;

import com.flagship.inventory.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Inventory Item Repository
 * <p>
 * Data access layer for InventoryItem entities. Provides custom queries for inventory management
 * operations.
 */
@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

  List<InventoryItem> findByProductIdOrderByLocation(Long productId);

  Optional<InventoryItem> findByProductIdAndLocation(Long productId, String location);

  List<InventoryItem> findBySkuOrderByLocation(String sku);

  List<InventoryItem> findByLocationOrderBySku(String location);

  Optional<InventoryItem> findBySkuAndLocation(String sku, String location);

  List<InventoryItem> findByAvailableQuantityGreaterThanOrderBySku(Integer availableQuantity);

  List<InventoryItem> findByAvailableQuantityLessThanEqualOrderBySku(Integer threshold);

  List<InventoryItem> findByAvailableQuantityOrderBySku(Integer availableQuantity);

  List<InventoryItem> findByReservedQuantityGreaterThanOrderBySku(Integer reservedQuantity);

  List<InventoryItem> findByReorderPointOrderBySku(Integer reorderPoint);

  List<InventoryItem> findByAvailableQuantityLessThanEqualAndReorderPointGreaterThanOrderBySku(
      Integer availableQuantity, Integer reorderPoint);

  List<InventoryItem> findByLastRestockedAfterOrderByLastRestockedDesc(LocalDateTime dateTime);

  List<InventoryItem> findByLastSoldAfterOrderByLastSoldDesc(LocalDateTime dateTime);

  @Query("SELECT ii FROM InventoryItem ii WHERE ii.product.id = :productId ORDER BY ii.location")
  List<InventoryItem> findByProductIdWithQuantities(@Param("productId") Long productId);

  @Query("SELECT ii FROM InventoryItem ii WHERE ii.location = :location ORDER BY ii.sku")
  List<InventoryItem> findByLocationWithQuantities(@Param("location") String location);

  @Query("SELECT ii FROM InventoryItem ii WHERE ii.sku = :sku ORDER BY ii.location")
  List<InventoryItem> findBySkuWithQuantities(@Param("sku") String sku);

  @Query("SELECT ii FROM InventoryItem ii WHERE ii.availableQuantity BETWEEN :minQuantity AND :maxQuantity ORDER BY ii.sku")
  List<InventoryItem> findByAvailableQuantityBetweenOrderBySku(
      @Param("minQuantity") Integer minQuantity,
      @Param("maxQuantity") Integer maxQuantity);

  @Query("SELECT ii FROM InventoryItem ii WHERE ii.reservedQuantity BETWEEN :minReservedQuantity AND :maxReservedQuantity ORDER BY ii.sku")
  List<InventoryItem> findByReservedQuantityBetweenOrderBySku(
      @Param("minReservedQuantity") Integer minReservedQuantity,
      @Param("maxReservedQuantity") Integer maxReservedQuantity);

  @Query("SELECT ii FROM InventoryItem ii WHERE ii.quantity BETWEEN :minTotalQuantity AND :maxTotalQuantity ORDER BY ii.sku")
  List<InventoryItem> findByTotalQuantityBetweenOrderBySku(
      @Param("minTotalQuantity") Integer minTotalQuantity,
      @Param("maxTotalQuantity") Integer maxTotalQuantity);

  @Query("SELECT ii FROM InventoryItem ii WHERE ii.product.id = :productId AND ii.availableQuantity BETWEEN :minQuantity AND :maxQuantity ORDER BY ii.location")
  List<InventoryItem> findByProductIdAndAvailableQuantityBetweenOrderByLocation(
      @Param("productId") Long productId,
      @Param("minQuantity") Integer minQuantity,
      @Param("maxQuantity") Integer maxQuantity);

  @Query("SELECT ii FROM InventoryItem ii WHERE ii.location = :location AND ii.availableQuantity BETWEEN :minQuantity AND :maxQuantity ORDER BY ii.sku")
  List<InventoryItem> findByLocationAndAvailableQuantityBetweenOrderBySku(
      @Param("location") String location,
      @Param("minQuantity") Integer minQuantity,
      @Param("maxQuantity") Integer maxQuantity);

  @Query("SELECT ii FROM InventoryItem ii WHERE ii.sku = :sku AND ii.availableQuantity BETWEEN :minQuantity AND :maxQuantity ORDER BY ii.location")
  List<InventoryItem> findBySkuAndAvailableQuantityBetweenOrderByLocation(@Param("sku") String sku,
      @Param("minQuantity") Integer minQuantity,
      @Param("maxQuantity") Integer maxQuantity);

  @Query("SELECT ii FROM InventoryItem ii WHERE ii.product.id = :productId AND ii.location = :location AND ii.availableQuantity BETWEEN :minQuantity AND :maxQuantity ORDER BY ii.sku")
  List<InventoryItem> findByProductIdAndLocationAndAvailableQuantityBetweenOrderBySku(
      @Param("productId") Long productId,
      @Param("location") String location,
      @Param("minQuantity") Integer minQuantity,
      @Param("maxQuantity") Integer maxQuantity);
}
