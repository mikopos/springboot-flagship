package com.flagship.inventory.controller;

import com.flagship.inventory.model.InventoryItem;
import com.flagship.inventory.model.Product;
import com.flagship.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Inventory Controller
 * <p>
 * REST API endpoints for inventory management operations. Provides CRUD operations for products and
 * inventory management.
 */
@Slf4j
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

  private final InventoryService inventoryService;

  @PostMapping("/products")
  @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
  public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
    log.info("Creating new product with SKU: {}", product.getSku());
    Product createdProduct = inventoryService.createProduct(product);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
  }

  @GetMapping("/products/{id}")
  public ResponseEntity<Product> getProductById(@PathVariable Long id) {
    log.debug("Getting product by ID: {}", id);

    Optional<Product> product = inventoryService.findProductById(id);
    return product.map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/products/sku/{sku}")
  public ResponseEntity<Product> getProductBySku(@PathVariable String sku) {
    log.debug("Getting product by SKU: {}", sku);

    Optional<Product> product = inventoryService.findProductBySku(sku);
    return product.map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/products/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
  public ResponseEntity<Product> updateProduct(@PathVariable Long id,
      @Valid @RequestBody Product updatedProduct) {
    log.info("Updating product with ID: {}", id);

    try {
      Product savedProduct = inventoryService.updateProduct(id, updatedProduct);
      return ResponseEntity.ok(savedProduct);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/products/{productId}/inventory")
  @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
  public ResponseEntity<InventoryItem> addInventory(@PathVariable Long productId,
      @RequestParam String location,
      @RequestParam Integer quantity) {
    log.info("Adding inventory for product ID: {} at location: {} with quantity: {}",
        productId, location, quantity);

    try {
      InventoryItem inventoryItem = inventoryService.addInventory(productId, location, quantity);
      return ResponseEntity.ok(inventoryItem);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/products/{productId}/reserve")
  @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER') or hasRole('ORDER_MANAGER')")
  public ResponseEntity<InventoryItem> reserveInventory(@PathVariable Long productId,
      @RequestParam String location,
      @RequestParam Integer quantity,
      @RequestParam Long orderId) {
    log.info(
        "Reserving inventory for product ID: {} at location: {} with quantity: {} for order: {}",
        productId, location, quantity, orderId);

    try {
      InventoryItem inventoryItem = inventoryService.reserveInventory(productId, location, quantity,
          orderId);
      return ResponseEntity.ok(inventoryItem);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    } catch (IllegalStateException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PostMapping("/products/{productId}/release")
  @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER') or hasRole('ORDER_MANAGER')")
  public ResponseEntity<InventoryItem> releaseReservedInventory(@PathVariable Long productId,
      @RequestParam String location,
      @RequestParam Integer quantity,
      @RequestParam Long orderId) {
    log.info(
        "Releasing reserved inventory for product ID: {} at location: {} with quantity: {} for order: {}",
        productId, location, quantity, orderId);

    try {
      InventoryItem inventoryItem = inventoryService.releaseReservedInventory(productId, location,
          quantity, orderId);
      return ResponseEntity.ok(inventoryItem);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    } catch (IllegalStateException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PostMapping("/products/{productId}/confirm")
  @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER') or hasRole('ORDER_MANAGER')")
  public ResponseEntity<InventoryItem> confirmReservedInventory(@PathVariable Long productId,
      @RequestParam String location,
      @RequestParam Integer quantity,
      @RequestParam Long orderId) {
    log.info(
        "Confirming reserved inventory for product ID: {} at location: {} with quantity: {} for order: {}",
        productId, location, quantity, orderId);

    try {
      InventoryItem inventoryItem = inventoryService.confirmReservedInventory(productId, location,
          quantity, orderId);
      return ResponseEntity.ok(inventoryItem);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    } catch (IllegalStateException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/products/{productId}/inventory")
  public ResponseEntity<List<InventoryItem>> getInventoryByProductId(@PathVariable Long productId) {
    log.debug("Getting inventory for product ID: {}", productId);

    List<InventoryItem> inventoryItems = inventoryService.getInventoryByProductId(productId);
    return ResponseEntity.ok(inventoryItems);
  }

  @GetMapping("/products/{productId}/inventory/{location}")
  public ResponseEntity<InventoryItem> getInventoryByProductIdAndLocation(
      @PathVariable Long productId,
      @PathVariable String location) {
    log.debug("Getting inventory for product ID: {} at location: {}", productId, location);

    Optional<InventoryItem> inventoryItem = inventoryService.getInventoryByProductIdAndLocation(
        productId, location);
    return inventoryItem.map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/products")
  public ResponseEntity<Page<Product>> getAllProducts(Pageable pageable) {
    log.debug("Getting all products with pagination: {}", pageable);

    Page<Product> products = inventoryService.findAllProducts(pageable);
    return ResponseEntity.ok(products);
  }

  @GetMapping("/products/category/{category}")
  public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
    log.debug("Getting products by category: {}", category);

    List<Product> products = inventoryService.findProductsByCategory(category);
    return ResponseEntity.ok(products);
  }

  @GetMapping("/products/status/{status}")
  public ResponseEntity<List<Product>> getProductsByStatus(
      @PathVariable Product.ProductStatus status) {
    log.debug("Getting products by status: {}", status);

    List<Product> products = inventoryService.findProductsByStatus(status);
    return ResponseEntity.ok(products);
  }

  @GetMapping("/products/low-stock")
  @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
  public ResponseEntity<List<Product>> getLowStockProducts(
      @RequestParam(defaultValue = "10") int threshold) {
    log.debug("Getting low stock products with threshold: {}", threshold);

    List<Product> products = inventoryService.findLowStockProducts(threshold);
    return ResponseEntity.ok(products);
  }

  @GetMapping("/products/out-of-stock")
  @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
  public ResponseEntity<List<Product>> getOutOfStockProducts() {
    log.debug("Getting out of stock products");

    List<Product> products = inventoryService.findOutOfStockProducts();
    return ResponseEntity.ok(products);
  }

  @GetMapping("/health")
  public ResponseEntity<String> health() {
    return ResponseEntity.ok("Inventory service is healthy");
  }
}
