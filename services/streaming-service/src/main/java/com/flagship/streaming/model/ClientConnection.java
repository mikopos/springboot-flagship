package com.flagship.streaming.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.FluxSink;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client Connection
 * <p>
 * Represents a client connection for streaming events. This model manages the connection state and
 * event filtering.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientConnection {

  private String id;
  private String userId;
  private String sessionId;
  private String ipAddress;
  private String userAgent;
  private LocalDateTime connectedAt;
  private LocalDateTime lastActivity;
  private FluxSink<StreamEvent> eventSink;
  private Set<String> subscribedEventTypes;
  private Set<String> subscribedSources;
  private boolean isActive;

  public static ClientConnection create(ServerRequest request, FluxSink<StreamEvent> eventSink) {
    return ClientConnection.builder()
        .id(java.util.UUID.randomUUID().toString())
        .connectedAt(LocalDateTime.now())
        .lastActivity(LocalDateTime.now())
        .eventSink(eventSink)
        .subscribedEventTypes(ConcurrentHashMap.newKeySet())
        .subscribedSources(ConcurrentHashMap.newKeySet())
        .isActive(true)
        .build();
  }


  public void updateActivity() {
    this.lastActivity = LocalDateTime.now();
  }

  public void subscribeToEventType(String eventType) {
    this.subscribedEventTypes.add(eventType);
  }

  public void unsubscribeFromEventType(String eventType) {
    this.subscribedEventTypes.remove(eventType);
  }

  public void subscribeToSource(String source) {
    this.subscribedSources.add(source);
  }

  public void unsubscribeFromSource(String source) {
    this.subscribedSources.remove(source);
  }

  public boolean isSubscribedToEventType(String eventType) {
    return subscribedEventTypes.isEmpty() || subscribedEventTypes.contains(eventType);
  }

  public boolean isSubscribedToSource(String source) {
    return subscribedSources.isEmpty() || subscribedSources.contains(source);
  }

  public boolean shouldReceiveEvent(StreamEvent event) {
    if (!isActive) {
      return false;
    }

    if (!isSubscribedToEventType(event.getType())) {
      return false;
    }

    if (!isSubscribedToSource(event.getSource())) {
      return false;
    }

    if (event.getUserId() != null && !event.getUserId().equals(userId)) {
      return false;
    }

    if (event.getSessionId() != null && !event.getSessionId().equals(sessionId)) {
      return false;
    }

    return true;
  }

  public void sendEvent(StreamEvent event) {
    if (shouldReceiveEvent(event)) {
      try {
        eventSink.next(event);
        updateActivity();
      } catch (Exception e) {
        isActive = false;
      }
    }
  }

  public void close() {
    isActive = false;
    try {
      eventSink.complete();
    } catch (Exception e) {
      // Ignore errors when closing
    }
  }
}
