package com.flagship.inventory.service;

import com.flagship.inventory.model.Product;
import com.flagship.inventory.model.ProductEvent;
import com.flagship.inventory.repository.ProductEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Product Event Service
 * <p>
 * Service for managing product events and audit logs. Provides functionality to log and retrieve
 * product events.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductEventService {

  private final ProductEventRepository productEventRepository;

  public void logEvent(Product product, ProductEvent.EventType eventType, String description,
      Integer previousQuantity, Integer newQuantity) {
    ProductEvent event = ProductEvent.builder()
        .product(product)
        .eventType(eventType)
        .description(description)
        .previousQuantity(previousQuantity)
        .newQuantity(newQuantity)
        .quantityChange(
            newQuantity != null && previousQuantity != null ? newQuantity - previousQuantity : null)
        .sku(product.getSku())
        .timestamp(LocalDateTime.now())
        .build();

    productEventRepository.save(event);
    log.debug("Logged product event: {} for product: {}", eventType, product.getId());
  }

  public void logEvent(Product product, ProductEvent.EventType eventType, String description,
      Integer previousQuantity, Integer newQuantity, String metadata) {
    ProductEvent event = ProductEvent.builder()
        .product(product)
        .eventType(eventType)
        .description(description)
        .previousQuantity(previousQuantity)
        .newQuantity(newQuantity)
        .quantityChange(
            newQuantity != null && previousQuantity != null ? newQuantity - previousQuantity : null)
        .sku(product.getSku())
        .metadata(metadata)
        .timestamp(LocalDateTime.now())
        .build();

    productEventRepository.save(event);
    log.debug("Logged product event: {} for product: {} with metadata", eventType, product.getId());
  }

  public void logEvent(Product product, ProductEvent.EventType eventType, String description,
      Integer previousQuantity, Integer newQuantity, String location) {
    ProductEvent event = ProductEvent.builder()
        .product(product)
        .eventType(eventType)
        .description(description)
        .previousQuantity(previousQuantity)
        .newQuantity(newQuantity)
        .quantityChange(
            newQuantity != null && previousQuantity != null ? newQuantity - previousQuantity : null)
        .sku(product.getSku())
        .location(location)
        .timestamp(LocalDateTime.now())
        .build();

    productEventRepository.save(event);
    log.debug("Logged product event: {} for product: {} at location: {}", eventType,
        product.getId(), location);
  }

  public void logEvent(Product product, ProductEvent.EventType eventType, String description,
      Integer previousQuantity, Integer newQuantity, String location, Long orderId) {
    ProductEvent event = ProductEvent.builder()
        .product(product)
        .eventType(eventType)
        .description(description)
        .previousQuantity(previousQuantity)
        .newQuantity(newQuantity)
        .quantityChange(
            newQuantity != null && previousQuantity != null ? newQuantity - previousQuantity : null)
        .sku(product.getSku())
        .location(location)
        .orderId(orderId)
        .timestamp(LocalDateTime.now())
        .build();

    productEventRepository.save(event);
    log.debug("Logged product event: {} for product: {} at location: {} for order: {}",
        eventType, product.getId(), location, orderId);
  }

  @Transactional(readOnly = true)
  public List<ProductEvent> getProductEvents(Long productId) {
    return productEventRepository.findByProductIdOrderByTimestampDesc(productId);
  }

  @Transactional(readOnly = true)
  public List<ProductEvent> getProductEventsByType(Long productId,
      ProductEvent.EventType eventType) {
    return productEventRepository.findByProductIdAndEventTypeOrderByTimestampDesc(productId,
        eventType);
  }

  @Transactional(readOnly = true)
  public List<ProductEvent> getProductEventsByDateRange(Long productId, LocalDateTime startDate,
      LocalDateTime endDate) {
    return productEventRepository.findByProductIdAndTimestampBetweenOrderByTimestampDesc(productId,
        startDate, endDate);
  }

  @Transactional(readOnly = true)
  public List<ProductEvent> getRecentProductEvents(Long productId, int limit) {
    return productEventRepository.findTopByProductIdOrderByTimestampDesc(productId, limit);
  }

  @Transactional(readOnly = true)
  public List<ProductEvent> getProductEventsByEventType(ProductEvent.EventType eventType) {
    return productEventRepository.findByEventTypeOrderByTimestampDesc(eventType);
  }

  @Transactional(readOnly = true)
  public List<ProductEvent> getProductEventsByLocation(String location) {
    return productEventRepository.findByLocationOrderByTimestampDesc(location);
  }

  @Transactional(readOnly = true)
  public List<ProductEvent> getProductEventsBySku(String sku) {
    return productEventRepository.findBySkuOrderByTimestampDesc(sku);
  }

  @Transactional(readOnly = true)
  public List<ProductEvent> getProductEventsByOrderId(Long orderId) {
    return productEventRepository.findByOrderIdOrderByTimestampDesc(orderId);
  }

  @Transactional(readOnly = true)
  public List<ProductEvent> getProductEventsByDateRange(LocalDateTime startDate,
      LocalDateTime endDate) {
    return productEventRepository.findByTimestampBetweenOrderByTimestampDesc(startDate, endDate);
  }
}
