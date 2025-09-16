package com.flagship.streaming.service;

import com.flagship.streaming.model.ClientConnection;
import com.flagship.streaming.model.StreamEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Streaming Service
 * <p>
 * Service for managing real-time event streaming via Server-Sent Events. Handles client
 * connections, event broadcasting, and Kafka event consumption.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StreamingService {

  private final Map<String, ClientConnection> clientConnections = new ConcurrentHashMap<>();
  private final Sinks.Many<StreamEvent> eventSink = Sinks.many().multicast().onBackpressureBuffer();

  public Flux<StreamEvent> createConnection(ClientConnection connection) {
    log.info("Creating new client connection: {}", connection.getId());

    clientConnections.put(connection.getId(), connection);

    return eventSink.asFlux()
        .filter(connection::shouldReceiveEvent)
        .doOnNext(connection::sendEvent)
        .doOnCancel(() -> {
          log.info("Client connection cancelled: {}", connection.getId());
          removeConnection(connection.getId());
        })
        .doOnError(error -> {
          log.error("Error in client connection: {}", connection.getId(), error);
          removeConnection(connection.getId());
        })
        .doOnComplete(() -> {
          log.info("Client connection completed: {}", connection.getId());
          removeConnection(connection.getId());
        });
  }

  public void removeConnection(String connectionId) {
    ClientConnection connection = clientConnections.remove(connectionId);
    if (connection != null) {
      connection.close();
      log.info("Removed client connection: {}", connectionId);
    }
  }

  public void broadcastEvent(StreamEvent event) {
    log.debug("Broadcasting event: {} to {} clients", event.getType(), clientConnections.size());

    try {
      eventSink.tryEmitNext(event);
    } catch (Exception e) {
      log.error("Failed to broadcast event: {}", event.getType(), e);
    }
  }

  public void broadcastEventToUser(StreamEvent event, String userId) {
    log.debug("Broadcasting event: {} to user: {}", event.getType(), userId);

    event.setUserId(userId);
    broadcastEvent(event);
  }

  public void broadcastEventToSession(StreamEvent event, String sessionId) {
    log.debug("Broadcasting event: {} to session: {}", event.getType(), sessionId);

    event.setSessionId(sessionId);
    broadcastEvent(event);
  }

  public int getActiveConnectionCount() {
    return (int) clientConnections.values().stream()
        .filter(ClientConnection::isActive)
        .count();
  }

  public int getConnectionCountForUser(String userId) {
    return (int) clientConnections.values().stream()
        .filter(connection -> userId.equals(connection.getUserId()))
        .filter(ClientConnection::isActive)
        .count();
  }

  public void cleanupInactiveConnections() {
    LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);

    clientConnections.entrySet().removeIf(entry -> {
      ClientConnection connection = entry.getValue();
      if (!connection.isActive() || connection.getLastActivity().isBefore(cutoff)) {
        connection.close();
        log.info("Cleaned up inactive connection: {}", entry.getKey());
        return true;
      }
      return false;
    });
  }

  public void sendHeartbeat() {
    StreamEvent heartbeat = StreamEvent.of("heartbeat", "streaming-service",
        Map.of("timestamp", LocalDateTime.now()));
    broadcastEvent(heartbeat);
  }

  @KafkaListener(topics = "user-events", groupId = "streaming-service")
  public void handleUserEvent(@Payload Object event,
      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
    log.debug("Received user event from topic: {}", topic);

    try {
      StreamEvent streamEvent = StreamEvent.of("user-event", "user-service", Map.of("data", event));
      broadcastEvent(streamEvent);
    } catch (Exception e) {
      log.error("Failed to process user event", e);
    }
  }

  @KafkaListener(topics = "order-events", groupId = "streaming-service")
  public void handleOrderEvent(@Payload Object event,
      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
    log.debug("Received order event from topic: {}", topic);

    try {
      StreamEvent streamEvent = StreamEvent.of("order-event", "order-service",
          Map.of("data", event));
      broadcastEvent(streamEvent);
    } catch (Exception e) {
      log.error("Failed to process order event", e);
    }
  }

  @KafkaListener(topics = "payment-events", groupId = "streaming-service")
  public void handlePaymentEvent(@Payload Object event,
      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
    log.debug("Received payment event from topic: {}", topic);

    try {
      StreamEvent streamEvent = StreamEvent.of("payment-event", "payment-service",
          Map.of("data", event));
      broadcastEvent(streamEvent);
    } catch (Exception e) {
      log.error("Failed to process payment event", e);
    }
  }

  @KafkaListener(topics = "inventory-events", groupId = "streaming-service")
  public void handleInventoryEvent(@Payload Object event,
      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
    log.debug("Received inventory event from topic: {}", topic);

    try {
      StreamEvent streamEvent = StreamEvent.of("inventory-event", "inventory-service",
          Map.of("data", event));
      broadcastEvent(streamEvent);
    } catch (Exception e) {
      log.error("Failed to process inventory event", e);
    }
  }

  public Flux<StreamEvent> createHeartbeatFlux() {
    return Flux.interval(Duration.ofSeconds(30))
        .map(tick -> StreamEvent.of("heartbeat", "streaming-service",
            Map.of("timestamp", LocalDateTime.now())));
  }
}
