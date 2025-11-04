package com.flagship.gateway.filter;

import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import com.flagship.gateway.service.JwtService;

/**
 * Logging Filter for API Gateway
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoggingFilter implements GlobalFilter, Ordered {

  private static final String START_TIME = "startTime";
  private static final String USER_ID = "userId";

  private final JwtService jwtService;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    ServerHttpResponse response = exchange.getResponse();

    exchange.getAttributes().put(START_TIME, Instant.now());

    return extractUserId(request)
        .doOnNext(userId -> {
          if (userId != null) {
            exchange.getAttributes().put(USER_ID, userId);
          }
          logRequest(request, userId);
        })
        .then(chain.filter(exchange))
        .then(Mono.fromRunnable(() -> logResponse(exchange, response)));
  }

  private void logRequest(ServerHttpRequest request, String userId) {
    log.info("Incoming Request: {} {} from {} - User: {} - Headers: {}",
        request.getMethod(),
        request.getURI(),
        request.getRemoteAddress(),
        userId != null ? userId : "anonymous",
        request.getHeaders().toSingleValueMap()
    );
  }

  private void logResponse(ServerWebExchange exchange, ServerHttpResponse response) {
    Instant startTime = exchange.getAttribute(START_TIME);
    String userId = exchange.getAttribute(USER_ID);

    if (startTime != null) {
      Duration duration = Duration.between(startTime, Instant.now());

      log.info("Outgoing Response: {} {} - Status: {} - Duration: {}ms - User: {}",
          exchange.getRequest().getMethod(),
          exchange.getRequest().getURI(),
          response.getStatusCode(),
          duration.toMillis(),
          userId != null ? userId : "anonymous"
      );
    }
  }

  private Mono<String> extractUserId(ServerHttpRequest request) {
    String authHeader = request.getHeaders().getFirst("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);
      return jwtService.extractUserId(token)
          .doOnNext(userId -> {
            if (userId != null) {
              log.debug("Successfully extracted user ID: {} from JWT token", userId);
            } else {
              log.debug("No user ID found in JWT token");
            }
          })
          .doOnError(error -> log.warn("Failed to extract user ID from JWT token: {}", error.getMessage()))
          .onErrorReturn(null);
    }
    return Mono.just(null);
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }
}
