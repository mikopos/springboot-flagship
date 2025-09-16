package com.flagship.streaming.controller;

import com.flagship.streaming.model.ClientConnection;
import com.flagship.streaming.model.StreamEvent;
import com.flagship.streaming.service.StreamingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * Streaming Controller
 * <p>
 * REST API endpoints for real-time event streaming. Provides Server-Sent Events (SSE) endpoints for
 * real-time communication.
 */
@Slf4j
@RestController
@RequestMapping("/api/stream")
@RequiredArgsConstructor
public class StreamingController {

  private final StreamingService streamingService;

  @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Mono<ServerResponse> streamEvents(ServerRequest request,
      @AuthenticationPrincipal Jwt jwt) {
    log.info("Creating SSE stream for user: {}", jwt.getSubject());

    ClientConnection connection = ClientConnection.create(request, null);
    connection.setUserId(jwt.getSubject());

    Flux<StreamEvent> eventStream = streamingService.createConnection(connection)
        .mergeWith(streamingService.createHeartbeatFlux())
        .onErrorResume(error -> {
          log.error("Error in event stream", error);
          return Flux.empty();
        });

    return ServerResponse.ok()
        .contentType(MediaType.TEXT_EVENT_STREAM)
        .header("Cache-Control", "no-cache")
        .header("Connection", "keep-alive")
        .body(eventStream.map(StreamEvent::toSSE), String.class);
  }

  @GetMapping(value = "/events/types/{eventTypes}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Mono<ServerResponse> streamEventsByType(@PathVariable String eventTypes,
      ServerRequest request,
      @AuthenticationPrincipal Jwt jwt) {
    log.info("Creating SSE stream for user: {} with event types: {}", jwt.getSubject(), eventTypes);

    ClientConnection connection = ClientConnection.create(request, null);
    connection.setUserId(jwt.getSubject());

    String[] types = eventTypes.split(",");
    for (String type : types) {
      connection.subscribeToEventType(type.trim());
    }

    Flux<StreamEvent> eventStream = streamingService.createConnection(connection)
        .mergeWith(streamingService.createHeartbeatFlux())
        .onErrorResume(error -> {
          log.error("Error in event stream", error);
          return Flux.empty();
        });

    return ServerResponse.ok()
        .contentType(MediaType.TEXT_EVENT_STREAM)
        .header("Cache-Control", "no-cache")
        .header("Connection", "keep-alive")
        .body(eventStream.map(StreamEvent::toSSE), String.class);
  }

  @GetMapping(value = "/events/sources/{sources}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Mono<ServerResponse> streamEventsBySource(@PathVariable String sources,
      ServerRequest request,
      @AuthenticationPrincipal Jwt jwt) {
    log.info("Creating SSE stream for user: {} with sources: {}", jwt.getSubject(), sources);

    ClientConnection connection = ClientConnection.create(request, null);
    connection.setUserId(jwt.getSubject());

    String[] sourceArray = sources.split(",");
    for (String source : sourceArray) {
      connection.subscribeToSource(source.trim());
    }

    Flux<StreamEvent> eventStream = streamingService.createConnection(connection)
        .mergeWith(streamingService.createHeartbeatFlux())
        .onErrorResume(error -> {
          log.error("Error in event stream", error);
          return Flux.empty();
        });

    return ServerResponse.ok()
        .contentType(MediaType.TEXT_EVENT_STREAM)
        .header("Cache-Control", "no-cache")
        .header("Connection", "keep-alive")
        .body(eventStream.map(StreamEvent::toSSE), String.class);
  }


  @GetMapping(value = "/events/user", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Mono<ServerResponse> streamUserEvents(ServerRequest request,
      @AuthenticationPrincipal Jwt jwt) {
    log.info("Creating user-specific SSE stream for user: {}", jwt.getSubject());

    ClientConnection connection = ClientConnection.create(request, null);
    connection.setUserId(jwt.getSubject());

    Flux<StreamEvent> eventStream = streamingService.createConnection(connection)
        .mergeWith(streamingService.createHeartbeatFlux())
        .onErrorResume(error -> {
          log.error("Error in user event stream", error);
          return Flux.empty();
        });

    return ServerResponse.ok()
        .contentType(MediaType.TEXT_EVENT_STREAM)
        .header("Cache-Control", "no-cache")
        .header("Connection", "keep-alive")
        .body(eventStream.map(StreamEvent::toSSE), String.class);
  }

  @GetMapping(value = "/events/session/{sessionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Mono<ServerResponse> streamSessionEvents(@PathVariable String sessionId,
      ServerRequest request,
      @AuthenticationPrincipal Jwt jwt) {
    log.info("Creating session-specific SSE stream for user: {} and session: {}", jwt.getSubject(),
        sessionId);

    ClientConnection connection = ClientConnection.create(request, null);
    connection.setUserId(jwt.getSubject());
    connection.setSessionId(sessionId);

    Flux<StreamEvent> eventStream = streamingService.createConnection(connection)
        .mergeWith(streamingService.createHeartbeatFlux())
        .onErrorResume(error -> {
          log.error("Error in session event stream", error);
          return Flux.empty();
        });

    return ServerResponse.ok()
        .contentType(MediaType.TEXT_EVENT_STREAM)
        .header("Cache-Control", "no-cache")
        .header("Connection", "keep-alive")
        .body(eventStream.map(StreamEvent::toSSE), String.class);
  }

  @GetMapping("/stats")
  public ResponseEntity<Map<String, Object>> getStats() {
    Map<String, Object> stats = Map.of(
        "activeConnections", streamingService.getActiveConnectionCount(),
        "timestamp", java.time.LocalDateTime.now()
    );

    return ResponseEntity.ok(stats);
  }

  @GetMapping("/stats/user/{userId}")
  public ResponseEntity<Map<String, Object>> getUserStats(@PathVariable String userId) {
    Map<String, Object> stats = Map.of(
        "userId", userId,
        "connectionCount", streamingService.getConnectionCountForUser(userId),
        "timestamp", java.time.LocalDateTime.now()
    );

    return ResponseEntity.ok(stats);
  }

  @GetMapping("/health")
  public ResponseEntity<String> health() {
    return ResponseEntity.ok("Streaming service is healthy");
  }
}
