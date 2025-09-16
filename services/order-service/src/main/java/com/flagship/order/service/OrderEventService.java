package com.flagship.order.service;

import com.flagship.order.model.Order;
import com.flagship.order.model.OrderEvent;
import com.flagship.order.repository.OrderEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Order Event Service
 * <p>
 * Service for managing order events and audit logs. Provides functionality to log and retrieve
 * order events.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderEventService {

  private final OrderEventRepository orderEventRepository;

  public void logEvent(Order order, OrderEvent.EventType eventType, String description,
      String previousStatus, String newStatus) {
    OrderEvent event = OrderEvent.builder()
        .order(order)
        .eventType(eventType)
        .description(description)
        .previousStatus(previousStatus)
        .newStatus(newStatus)
        .userId(order.getUserId())
        .timestamp(LocalDateTime.now())
        .build();

    orderEventRepository.save(event);
    log.debug("Logged order event: {} for order: {}", eventType, order.getId());
  }

  public void logEvent(Order order, OrderEvent.EventType eventType, String description,
      String previousStatus, String newStatus, String metadata) {
    OrderEvent event = OrderEvent.builder()
        .order(order)
        .eventType(eventType)
        .description(description)
        .previousStatus(previousStatus)
        .newStatus(newStatus)
        .metadata(metadata)
        .userId(order.getUserId())
        .timestamp(LocalDateTime.now())
        .build();

    orderEventRepository.save(event);
    log.debug("Logged order event: {} for order: {} with metadata", eventType, order.getId());
  }

  public void logEvent(Order order, OrderEvent.EventType eventType, String description,
      String previousStatus, String newStatus, String ipAddress, String userAgent) {
    OrderEvent event = OrderEvent.builder()
        .order(order)
        .eventType(eventType)
        .description(description)
        .previousStatus(previousStatus)
        .newStatus(newStatus)
        .ipAddress(ipAddress)
        .userAgent(userAgent)
        .userId(order.getUserId())
        .timestamp(LocalDateTime.now())
        .build();

    orderEventRepository.save(event);
    log.debug("Logged order event: {} for order: {} with IP and user agent", eventType,
        order.getId());
  }

  @Transactional(readOnly = true)
  public List<OrderEvent> getOrderEvents(Long orderId) {
    return orderEventRepository.findByOrderIdOrderByTimestampDesc(orderId);
  }

  @Transactional(readOnly = true)
  public List<OrderEvent> getOrderEventsByType(Long orderId, OrderEvent.EventType eventType) {
    return orderEventRepository.findByOrderIdAndEventTypeOrderByTimestampDesc(orderId, eventType);
  }

  @Transactional(readOnly = true)
  public List<OrderEvent> getOrderEventsByDateRange(Long orderId, LocalDateTime startDate,
      LocalDateTime endDate) {
    return orderEventRepository.findByOrderIdAndTimestampBetweenOrderByTimestampDesc(orderId,
        startDate, endDate);
  }

  @Transactional(readOnly = true)
  public List<OrderEvent> getRecentOrderEvents(Long orderId, int limit) {
    return orderEventRepository.findTopByOrderIdOrderByTimestampDesc(orderId, limit);
  }

  @Transactional(readOnly = true)
  public List<OrderEvent> getOrderEventsByEventType(OrderEvent.EventType eventType) {
    return orderEventRepository.findByEventTypeOrderByTimestampDesc(eventType);
  }

  @Transactional(readOnly = true)
  public List<OrderEvent> getOrderEventsByUserId(Long userId) {
    return orderEventRepository.findByUserIdOrderByTimestampDesc(userId);
  }

  @Transactional(readOnly = true)
  public List<OrderEvent> getOrderEventsByIpAddress(String ipAddress) {
    return orderEventRepository.findByIpAddressOrderByTimestampDesc(ipAddress);
  }

  @Transactional(readOnly = true)
  public List<OrderEvent> getOrderEventsByDateRange(LocalDateTime startDate,
      LocalDateTime endDate) {
    return orderEventRepository.findByTimestampBetweenOrderByTimestampDesc(startDate, endDate);
  }
}
