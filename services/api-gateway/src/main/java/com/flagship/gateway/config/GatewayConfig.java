package com.flagship.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Gateway Configuration
 * <p>
 * Defines routing rules for the API Gateway, including: - Service routing with load balancing -
 * Authentication requirements - Rate limiting - Circuit breaker patterns - Request/response
 * transformation
 */
@Configuration
public class GatewayConfig {

  @Value("${spring.data.redis.host:}")
  private String redisHost;

  @Bean
  public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    return builder.routes()
        .route("user-service", r -> r
            .path("/api/users/**")
            .filters(f -> {
              f.addRequestHeader("X-Service", "user-service")
               .circuitBreaker(config -> config
                   .setName("user-service-cb")
                   .setFallbackUri("forward:/fallback/user-service"));
              
              // Rate limiting disabled for Railway deployment
              // if (isRedisEnabled()) {
              //   f.requestRateLimiter(config -> config
              //       .setRateLimiter(redisRateLimiter())
              //       .setKeyResolver(userKeyResolver()));
              // }
              
              return f;
            })
            .uri("lb://user-service"))

        .route("order-service", r -> r
            .path("/api/orders/**")
            .filters(f -> {
              f.addRequestHeader("X-Service", "order-service")
               .circuitBreaker(config -> config
                   .setName("order-service-cb")
                   .setFallbackUri("forward:/fallback/order-service"));
              
              // Rate limiting disabled for Railway deployment
              // if (isRedisEnabled()) {
              //   f.requestRateLimiter(config -> config
              //       .setRateLimiter(redisRateLimiter())
              //       .setKeyResolver(userKeyResolver()));
              // }
              
              return f;
            })
            .uri("lb://order-service"))

        .route("payment-service", r -> r
            .path("/api/payments/**")
            .filters(f -> {
              f.addRequestHeader("X-Service", "payment-service")
               .circuitBreaker(config -> config
                   .setName("payment-service-cb")
                   .setFallbackUri("forward:/fallback/payment-service"));
              
              // Rate limiting disabled for Railway deployment
              // if (isRedisEnabled()) {
              //   f.requestRateLimiter(config -> config
              //       .setRateLimiter(redisRateLimiter())
              //       .setKeyResolver(userKeyResolver()));
              // }
              
              return f;
            })
            .uri("lb://payment-service"))

        .route("inventory-service", r -> r
            .path("/api/inventory/**")
            .filters(f -> {
              f.addRequestHeader("X-Service", "inventory-service")
               .circuitBreaker(config -> config
                   .setName("inventory-service-cb")
                   .setFallbackUri("forward:/fallback/inventory-service"));
              
              // Rate limiting disabled for Railway deployment
              // if (isRedisEnabled()) {
              //   f.requestRateLimiter(config -> config
              //       .setRateLimiter(redisRateLimiter())
              //       .setKeyResolver(userKeyResolver()));
              // }
              
              return f;
            })
            .uri("lb://inventory-service"))

        .route("streaming-service", r -> r
            .path("/api/stream/**")
            .filters(f -> f
                .addRequestHeader("X-Service", "streaming-service")
                .circuitBreaker(config -> config
                    .setName("streaming-service-cb")
                    .setFallbackUri("forward:/fallback/streaming-service")))
            .uri("lb://streaming-service"))

        // Health Check Routes (No Authentication Required)
        .route("health-checks", r -> r
            .path("/actuator/health", "/actuator/info")
            .uri("lb://user-service"))

        .build();
  }

  @Bean
  @ConditionalOnExpression("!'${spring.data.redis.host:}'.isEmpty()")
  public RedisRateLimiter redisRateLimiter() {
    return new RedisRateLimiter(
        10,
        20,
        1);
  }

  @Bean
  @ConditionalOnExpression("!'${spring.data.redis.host:}'.isEmpty()")
  public KeyResolver userKeyResolver() {
    return exchange -> {
      String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
      if (authHeader != null && authHeader.startsWith("Bearer ")) {
        // In a real implementation, the JWT token should be decoded
        // For now, we'll use a simple approach
        return Mono.just("user-" + authHeader.hashCode());
      }
      return Mono.just("anonymous");
    };
  }

  private boolean isRedisEnabled() {
    return redisHost != null && !redisHost.trim().isEmpty();
  }
}
