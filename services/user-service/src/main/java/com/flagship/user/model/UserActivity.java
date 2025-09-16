package com.flagship.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * User Activity Entity
 * <p>
 * Tracks user activities and events for audit and analytics purposes. This entity stores
 * information about user actions and system events.
 */
@Entity
@Table(name = "user_activities", indexes = {
    @Index(name = "idx_user_activity_user_id", columnList = "user_id"),
    @Index(name = "idx_user_activity_type", columnList = "activity_type"),
    @Index(name = "idx_user_activity_timestamp", columnList = "timestamp")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "activity_type", nullable = false)
  private ActivityType activityType;

  @Column(name = "description")
  @NotBlank(message = "Activity description is required")
  private String description;

  @Column(name = "ip_address")
  private String ipAddress;

  @Column(name = "user_agent")
  private String userAgent;

  @Column(name = "metadata", columnDefinition = "TEXT")
  private String metadata;

  @CreationTimestamp
  @Column(name = "timestamp", nullable = false, updatable = false)
  private LocalDateTime timestamp;

  public enum ActivityType {
    LOGIN,
    LOGOUT,
    PROFILE_UPDATE,
    PASSWORD_CHANGE,
    EMAIL_VERIFICATION,
    ORDER_CREATED,
    ORDER_CANCELLED,
    PAYMENT_MADE,
    PREFERENCE_CHANGED,
    ACCOUNT_DEACTIVATED,
    ACCOUNT_ACTIVATED,
    SECURITY_EVENT,
    SYSTEM_EVENT
  }
}
