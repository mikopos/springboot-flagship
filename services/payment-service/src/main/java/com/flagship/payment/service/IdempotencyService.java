package com.flagship.payment.service;

import com.flagship.payment.model.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * Idempotency Service
 * <p>
 * Service for managing payment idempotency using Redis. Ensures that payment operations are
 * idempotent and prevents duplicate processing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

  private final RedisTemplate<String, Object> redisTemplate;
  private static final String IDEMPOTENCY_KEY_PREFIX = "payment:idempotency:";
  private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(24);

  public void storePaymentIdempotency(String idempotencyKey, Payment payment) {
    String key = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
    try {
      redisTemplate.opsForValue().set(key, payment, IDEMPOTENCY_TTL);
      log.debug("Stored payment idempotency key: {}", idempotencyKey);
    } catch (Exception e) {
      log.error("Failed to store payment idempotency key: {}", idempotencyKey, e);
    }
  }

  public Optional<Payment> getPaymentByIdempotencyKey(String idempotencyKey) {
    String key = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
    try {
      Object value = redisTemplate.opsForValue().get(key);
      if (value instanceof Payment) {
        log.debug("Retrieved payment by idempotency key: {}", idempotencyKey);
        return Optional.of((Payment) value);
      }
    } catch (Exception e) {
      log.error("Failed to retrieve payment by idempotency key: {}", idempotencyKey, e);
    }
    return Optional.empty();
  }

  public boolean existsIdempotencyKey(String idempotencyKey) {
    String key = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
    try {
      Boolean exists = redisTemplate.hasKey(key);
      return exists != null && exists;
    } catch (Exception e) {
      log.error("Failed to check idempotency key existence: {}", idempotencyKey, e);
      return false;
    }
  }

  public void deleteIdempotencyKey(String idempotencyKey) {
    String key = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
    try {
      redisTemplate.delete(key);
      log.debug("Deleted payment idempotency key: {}", idempotencyKey);
    } catch (Exception e) {
      log.error("Failed to delete payment idempotency key: {}", idempotencyKey, e);
    }
  }

  public void extendIdempotencyKeyTtl(String idempotencyKey, Duration duration) {
    String key = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
    try {
      redisTemplate.expire(key, duration);
      log.debug("Extended idempotency key TTL: {}", idempotencyKey);
    } catch (Exception e) {
      log.error("Failed to extend idempotency key TTL: {}", idempotencyKey, e);
    }
  }
}
