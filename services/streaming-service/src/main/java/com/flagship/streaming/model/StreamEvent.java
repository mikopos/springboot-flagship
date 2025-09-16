package com.flagship.streaming.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Stream Event
 * <p>
 * Represents an event that can be streamed to clients via Server-Sent Events. This model provides a
 * standardized format for all streaming events.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamEvent {

  private String id;
  private String type;
  private String source;
  private LocalDateTime timestamp;
  private Map<String, Object> data;
  private String userId;
  private String sessionId;
  private String correlationId;

  public static StreamEvent of(String type, String source, Map<String, Object> data) {
    return StreamEvent.builder()
        .id(java.util.UUID.randomUUID().toString())
        .type(type)
        .source(source)
        .timestamp(LocalDateTime.now())
        .data(data)
        .build();
  }

  public static StreamEvent of(String type, String source, Map<String, Object> data,
      String userId) {
    return StreamEvent.builder()
        .id(java.util.UUID.randomUUID().toString())
        .type(type)
        .source(source)
        .timestamp(LocalDateTime.now())
        .data(data)
        .userId(userId)
        .build();
  }

  public static StreamEvent of(String type, String source, Map<String, Object> data, String userId,
      String sessionId) {
    return StreamEvent.builder()
        .id(java.util.UUID.randomUUID().toString())
        .type(type)
        .source(source)
        .timestamp(LocalDateTime.now())
        .data(data)
        .userId(userId)
        .sessionId(sessionId)
        .build();
  }

  public String toSSE() {
    StringBuilder sse = new StringBuilder();
    sse.append("id: ").append(id).append("\n");
    sse.append("event: ").append(type).append("\n");
    sse.append("data: ").append(toJson()).append("\n\n");
    return sse.toString();
  }

  public String toJson() {
    try {
      com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
      return mapper.writeValueAsString(this);
    } catch (Exception e) {
      return "{}";
    }
  }
}
