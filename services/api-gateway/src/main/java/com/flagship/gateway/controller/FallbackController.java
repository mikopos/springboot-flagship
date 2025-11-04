package com.flagship.gateway.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Fallback Controller for Circuit Breaker
 * <p>
 * Provides fallback responses when services are unavailable due to circuit breaker activation. This
 * ensures graceful degradation and maintains service availability even when backend services fail.
 */
@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

  @GetMapping("/{serviceName}")
  public Mono<ResponseEntity<Map<String, Object>>> fallback(@PathVariable String serviceName) {
    log.warn("Circuit breaker activated for service: {}", serviceName);

    Map<String, Object> response = new HashMap<>();
    response.put("error", "Service Unavailable");
    response.put("message",
        "The " + serviceName + " service is currently unavailable. Please try again later.");
    response.put("timestamp", LocalDateTime.now());
    response.put("service", serviceName);
    response.put("status", "CIRCUIT_BREAKER_OPEN");

    return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
  }

  @GetMapping("/user-service")
  public Mono<ResponseEntity<Map<String, Object>>> userServiceFallback() {
    return fallback("user-service");
  }

  @GetMapping("/order-service")
  public Mono<ResponseEntity<Map<String, Object>>> orderServiceFallback() {
    return fallback("order-service");
  }

  @GetMapping("/payment-service")
  public Mono<ResponseEntity<Map<String, Object>>> paymentServiceFallback() {
    return fallback("payment-service");
  }

  @GetMapping("/inventory-service")
  public Mono<ResponseEntity<Map<String, Object>>> inventoryServiceFallback() {
    return fallback("inventory-service");
  }

  @GetMapping("/streaming-service")
  public Mono<ResponseEntity<Map<String, Object>>> streamingServiceFallback() {
    return fallback("streaming-service");
  }
}
