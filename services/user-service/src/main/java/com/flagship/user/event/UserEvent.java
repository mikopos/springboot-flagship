package com.flagship.user.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User Event
 * <p>
 * Represents a user-related event that can be published to Kafka. Used for event-driven
 * communication between microservices.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent {

  private Long userId;
  private String keycloakId;
  private String email;
  private UserEventType eventType;
  private LocalDateTime timestamp;
  private String metadata;

  public enum UserEventType {
    USER_CREATED,
    USER_UPDATED,
    USER_DEACTIVATED,
    USER_ACTIVATED,
    USER_LOGIN,
    USER_LOGOUT,
    PROFILE_UPDATED,
    PREFERENCES_CHANGED
  }
}
