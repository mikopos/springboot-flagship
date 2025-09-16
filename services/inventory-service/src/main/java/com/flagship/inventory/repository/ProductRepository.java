package com.flagship.inventory.repository;

import com.flagship.inventory.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Product Repository
 * <p>
 * Data access layer for Product entities. Provides custom queries for product management
 * operations.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  Optional<Product> findBySku(String sku);

  boolean existsBySku(String sku);

  List<Product> findByCategoryOrderByName(String category);

  List<Product> findByStatusOrderByName(Product.ProductStatus status);

  List<Product> findByBrandOrderByName(String brand);

  List<Product> findByNameContainingIgnoreCaseOrderByName(String name);

  List<Product> findByPriceBetweenOrderByPrice(BigDecimal minPrice, BigDecimal maxPrice);

  List<Product> findByCurrencyOrderByName(String currency);

  List<Product> findByIsDigitalTrueOrderByName();

  List<Product> findByIsDigitalFalseOrderByName();

  List<Product> findByRequiresShippingTrueOrderByName();

  List<Product> findByRequiresShippingFalseOrderByName();

  @Query("SELECT p FROM Product p WHERE p.id IN (" +
      "SELECT ii.product.id FROM InventoryItem ii " +
      "WHERE ii.availableQuantity <= :threshold " +
      "GROUP BY ii.product.id " +
      "HAVING SUM(ii.availableQuantity) <= :threshold)")
  List<Product> findLowStockProducts(@Param("threshold") int threshold);

  @Query("SELECT p FROM Product p WHERE p.id IN (" +
      "SELECT ii.product.id FROM InventoryItem ii " +
      "GROUP BY ii.product.id " +
      "HAVING SUM(ii.availableQuantity) = 0)")
  List<Product> findOutOfStockProducts();

  @Query("SELECT p FROM Product p WHERE p.category = :category AND p.price BETWEEN :minPrice AND :maxPrice ORDER BY p.name")
  List<Product> findByCategoryAndPriceBetweenOrderByName(@Param("category") String category,
      @Param("minPrice") java.math.BigDecimal minPrice,
      @Param("maxPrice") java.math.BigDecimal maxPrice);

  @Query("SELECT p FROM Product p WHERE p.brand = :brand AND p.status = :status ORDER BY p.name")
  List<Product> findByBrandAndStatusOrderByName(@Param("brand") String brand,
      @Param("status") Product.ProductStatus status);

  @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :namePattern, '%')) AND p.category = :category ORDER BY p.name")
  List<Product> findByNameContainingAndCategoryOrderByName(@Param("namePattern") String namePattern,
      @Param("category") String category);

  @Query("SELECT COUNT(p) FROM Product p WHERE p.category = :category")
  long countByCategory(@Param("category") String category);

  @Query("SELECT COUNT(p) FROM Product p WHERE p.status = :status")
  long countByStatus(@Param("status") Product.ProductStatus status);

  @Query("SELECT p FROM Product p WHERE p.taxCategory = :taxCategory ORDER BY p.name")
  List<Product> findByTaxCategoryOrderByName(@Param("taxCategory") String taxCategory);

  @Query("SELECT p FROM Product p WHERE p.metadata IS NOT NULL ORDER BY p.name")
  List<Product> findWithMetadataOrderByName();
}
