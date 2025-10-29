package com.flagship.inventory.service;

import com.flagship.inventory.event.InventoryEvent;
import com.flagship.inventory.model.InventoryItem;
import com.flagship.inventory.model.Product;
import com.flagship.inventory.model.ProductEvent;
import com.flagship.inventory.repository.InventoryItemRepository;
import com.flagship.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Inventory Service
 * <p>
 * Business logic layer for inventory management operations. Handles inventory CRUD operations,
 * stock management, and event publishing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {

  private final ProductRepository productRepository;
  private final InventoryItemRepository inventoryItemRepository;
  private final KafkaTemplate<String, InventoryEvent> kafkaTemplate;
  private final ProductEventService productEventService;

  @CacheEvict(value = "products", allEntries = true)
  public Product createProduct(Product product) {
    log.info("Creating new product with SKU: {}", product.getSku());

    // Validate that product doesn't already exist
    if (productRepository.existsBySku(product.getSku())) {
      throw new IllegalArgumentException("Product with SKU already exists: " + product.getSku());
    }

    Product savedProduct = productRepository.save(product);

    productEventService.logEvent(savedProduct, ProductEvent.EventType.PRODUCT_CREATED,
        "Product created", null, null);

    publishInventoryEvent(savedProduct, InventoryEvent.InventoryEventType.PRODUCT_CREATED);

    log.info("Product created successfully with ID: {}", savedProduct.getId());
    return savedProduct;
  }

  @Cacheable(value = "products", key = "#id")
  @Transactional(readOnly = true)
  public Optional<Product> findProductById(Long id) {
    return productRepository.findById(id);
  }

  @Cacheable(value = "products", key = "#sku")
  @Transactional(readOnly = true)
  public Optional<Product> findProductBySku(String sku) {
    return productRepository.findBySku(sku);
  }

  @CacheEvict(value = "products", key = "#id")
  public Product updateProduct(Long id, Product updatedProduct) {
    log.info("Updating product with ID: {}", id);

    Product existingProduct = productRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));

    existingProduct.setName(updatedProduct.getName());
    existingProduct.setDescription(updatedProduct.getDescription());
    existingProduct.setCategory(updatedProduct.getCategory());
    existingProduct.setBrand(updatedProduct.getBrand());
    existingProduct.setPrice(updatedProduct.getPrice());
    existingProduct.setCurrency(updatedProduct.getCurrency());
    existingProduct.setWeight(updatedProduct.getWeight());
    existingProduct.setDimensions(updatedProduct.getDimensions());
    existingProduct.setImageUrl(updatedProduct.getImageUrl());
    existingProduct.setStatus(updatedProduct.getStatus());
    existingProduct.setIsDigital(updatedProduct.getIsDigital());
    existingProduct.setRequiresShipping(updatedProduct.getRequiresShipping());
    existingProduct.setTaxCategory(updatedProduct.getTaxCategory());
    existingProduct.setMetadata(updatedProduct.getMetadata());

    Product savedProduct = productRepository.save(existingProduct);

    productEventService.logEvent(savedProduct, ProductEvent.EventType.PRODUCT_UPDATED,
        "Product updated", null, null);

    publishInventoryEvent(savedProduct, InventoryEvent.InventoryEventType.PRODUCT_UPDATED);

    log.info("Product updated successfully with ID: {}", savedProduct.getId());
    return savedProduct;
  }

  @CacheEvict(value = "products", key = "#productId")
  public InventoryItem addInventory(Long productId, String location, Integer quantity) {
    log.info("Adding inventory for product ID: {} at location: {} with quantity: {}",
        productId, location, quantity);

    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

    InventoryItem inventoryItem = inventoryItemRepository
        .findByProductIdAndLocation(productId, location)
        .orElse(InventoryItem.builder()
            .product(product)
            .sku(product.getSku())
            .location(location)
            .quantity(0)
            .reservedQuantity(0)
            .availableQuantity(0)
            .build());

    inventoryItem.addQuantity(quantity);

    InventoryItem savedItem = inventoryItemRepository.save(inventoryItem);

    productEventService.logEvent(product, ProductEvent.EventType.INVENTORY_ADDED,
        "Inventory added: " + quantity + " at " + location,
        savedItem.getQuantity() - quantity, savedItem.getQuantity());

    publishInventoryEvent(product, InventoryEvent.InventoryEventType.INVENTORY_ADDED);

    log.info("Inventory added successfully for product ID: {}", productId);
    return savedItem;
  }

  @CacheEvict(value = "products", key = "#productId")
  public InventoryItem reserveInventory(Long productId, String location, Integer quantity,
      Long orderId) {
    log.info(
        "Reserving inventory for product ID: {} at location: {} with quantity: {} for order: {}",
        productId, location, quantity, orderId);

    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

    InventoryItem inventoryItem = inventoryItemRepository
        .findByProductIdAndLocation(productId, location)
        .orElseThrow(() -> new IllegalArgumentException(
            "Inventory item not found for product: " + productId + " at location: " + location));

    boolean reserved = inventoryItem.reserveQuantity(quantity);
    if (!reserved) {
      throw new IllegalStateException("Insufficient inventory available for reservation");
    }

    InventoryItem savedItem = inventoryItemRepository.save(inventoryItem);

    productEventService.logEvent(product, ProductEvent.EventType.INVENTORY_RESERVED,
        "Inventory reserved: " + quantity + " for order: " + orderId,
        savedItem.getAvailableQuantity() + quantity, savedItem.getAvailableQuantity());

    publishInventoryEvent(product, InventoryEvent.InventoryEventType.INVENTORY_RESERVED);

    log.info("Inventory reserved successfully for product ID: {}", productId);
    return savedItem;
  }

  @CacheEvict(value = "products", key = "#productId")
  public InventoryItem releaseReservedInventory(Long productId, String location, Integer quantity,
      Long orderId) {
    log.info(
        "Releasing reserved inventory for product ID: {} at location: {} with quantity: {} for order: {}",
        productId, location, quantity, orderId);

    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

    InventoryItem inventoryItem = inventoryItemRepository
        .findByProductIdAndLocation(productId, location)
        .orElseThrow(() -> new IllegalArgumentException(
            "Inventory item not found for product: " + productId + " at location: " + location));

    boolean released = inventoryItem.releaseReservedQuantity(quantity);
    if (!released) {
      throw new IllegalStateException("Insufficient reserved inventory to release");
    }

    InventoryItem savedItem = inventoryItemRepository.save(inventoryItem);

    productEventService.logEvent(product, ProductEvent.EventType.INVENTORY_RELEASED,
        "Inventory released: " + quantity + " for order: " + orderId,
        savedItem.getAvailableQuantity() - quantity, savedItem.getAvailableQuantity());

    publishInventoryEvent(product, InventoryEvent.InventoryEventType.INVENTORY_RELEASED);

    log.info("Inventory released successfully for product ID: {}", productId);
    return savedItem;
  }

  @CacheEvict(value = "products", key = "#productId")
  public InventoryItem confirmReservedInventory(Long productId, String location, Integer quantity,
      Long orderId) {
    log.info(
        "Confirming reserved inventory for product ID: {} at location: {} with quantity: {} for order: {}",
        productId, location, quantity, orderId);

    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

    InventoryItem inventoryItem = inventoryItemRepository
        .findByProductIdAndLocation(productId, location)
        .orElseThrow(() -> new IllegalArgumentException(
            "Inventory item not found for product: " + productId + " at location: " + location));

    boolean confirmed = inventoryItem.confirmReservedQuantity(quantity);
    if (!confirmed) {
      throw new IllegalStateException("Insufficient reserved inventory to confirm");
    }

    InventoryItem savedItem = inventoryItemRepository.save(inventoryItem);

    productEventService.logEvent(product, ProductEvent.EventType.INVENTORY_CONFIRMED,
        "Inventory confirmed: " + quantity + " for order: " + orderId,
        savedItem.getQuantity() + quantity, savedItem.getQuantity());

    publishInventoryEvent(product, InventoryEvent.InventoryEventType.INVENTORY_CONFIRMED);

    log.info("Inventory confirmed successfully for product ID: {}", productId);
    return savedItem;
  }

  @Transactional(readOnly = true)
  public List<InventoryItem> getInventoryByProductId(Long productId) {
    return inventoryItemRepository.findByProductIdOrderByLocation(productId);
  }

  @Transactional(readOnly = true)
  public Optional<InventoryItem> getInventoryByProductIdAndLocation(Long productId,
      String location) {
    return inventoryItemRepository.findByProductIdAndLocation(productId, location);
  }

  @Transactional(readOnly = true)
  public List<Product> findAllProducts() {
    return productRepository.findAll();
  }

  @Transactional(readOnly = true)
  public Page<Product> findAllProducts(Pageable pageable) {
    return productRepository.findAll(pageable);
  }

  @Transactional(readOnly = true)
  public List<Product> findProductsByCategory(String category) {
    return productRepository.findByCategoryOrderByName(category);
  }

  @Transactional(readOnly = true)
  public List<Product> findProductsByStatus(Product.ProductStatus status) {
    return productRepository.findByStatusOrderByName(status);
  }

  @Transactional(readOnly = true)
  public List<Product> findLowStockProducts(int threshold) {
    return productRepository.findLowStockProducts(threshold);
  }

  @Transactional(readOnly = true)
  public List<Product> findOutOfStockProducts() {
    return productRepository.findOutOfStockProducts();
  }

  private void publishInventoryEvent(Product product, InventoryEvent.InventoryEventType eventType) {
    try {
      // Create lightweight event with only essential data
      InventoryEvent event = InventoryEvent.builder()
          .productId(product.getId())
          .sku(product.getSku())
          .eventType(eventType)
          .timestamp(LocalDateTime.now())
          .productName(product.getName())
          .category(product.getCategory())
          .status(product.getStatus().toString())
          .totalQuantity(product.getTotalQuantity())
          .availableQuantity(product.getTotalAvailableQuantity())
          .build();

      kafkaTemplate.send("inventory-events", event);
      log.debug("Published inventory event: {} for product: {}", eventType, product.getId());
    } catch (Exception e) {
      log.error("Failed to publish inventory event: {} for product: {}", eventType, product.getId(),
          e);
    }
  }
}
