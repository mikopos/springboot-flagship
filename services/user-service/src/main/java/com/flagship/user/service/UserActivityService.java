package com.flagship.user.service;

import com.flagship.user.model.User;
import com.flagship.user.model.UserActivity;
import com.flagship.user.repository.UserActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User Activity Service
 * <p>
 * Service for managing user activities and audit logs. Provides functionality to log and retrieve
 * user activities.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserActivityService {

  private final UserActivityRepository userActivityRepository;

  public void logActivity(User user, ActivityType activityType, String description,
      String ipAddress, String userAgent) {
    UserActivity activity = UserActivity.builder()
        .user(user)
        .activityType(convertActivityType(activityType))
        .description(description)
        .ipAddress(ipAddress)
        .userAgent(userAgent)
        .timestamp(LocalDateTime.now())
        .build();

    userActivityRepository.save(activity);
    log.debug("Logged activity: {} for user: {}", activityType, user.getId());
  }

  public void logActivity(User user, ActivityType activityType, String description,
      String ipAddress, String userAgent, String metadata) {
    UserActivity activity = UserActivity.builder()
        .user(user)
        .activityType(convertActivityType(activityType))
        .description(description)
        .ipAddress(ipAddress)
        .userAgent(userAgent)
        .metadata(metadata)
        .timestamp(LocalDateTime.now())
        .build();

    userActivityRepository.save(activity);
    log.debug("Logged activity: {} for user: {} with metadata", activityType, user.getId());
  }

  @Transactional(readOnly = true)
  public List<UserActivity> getUserActivities(Long userId) {
    return userActivityRepository.findByUserIdOrderByTimestampDesc(userId);
  }

  @Transactional(readOnly = true)
  public List<UserActivity> getUserActivitiesByType(Long userId, ActivityType activityType) {
    return userActivityRepository.findByUserIdAndActivityTypeOrderByTimestampDesc(
        userId, convertActivityType(activityType));
  }

  @Transactional(readOnly = true)
  public List<UserActivity> getUserActivitiesByDateRange(Long userId, LocalDateTime startDate,
      LocalDateTime endDate) {
    return userActivityRepository.findByUserIdAndTimestampBetweenOrderByTimestampDesc(
        userId, startDate, endDate);
  }

  @Transactional(readOnly = true)
  public List<UserActivity> getRecentUserActivities(Long userId, int limit) {
    return userActivityRepository.findTopByUserIdOrderByTimestampDesc(userId, limit);
  }

  private UserActivity.ActivityType convertActivityType(ActivityType activityType) {
    return switch (activityType) {
      case LOGIN -> UserActivity.ActivityType.LOGIN;
      case LOGOUT -> UserActivity.ActivityType.LOGOUT;
      case PROFILE_UPDATE -> UserActivity.ActivityType.PROFILE_UPDATE;
      case PASSWORD_CHANGE -> UserActivity.ActivityType.PASSWORD_CHANGE;
      case EMAIL_VERIFICATION -> UserActivity.ActivityType.EMAIL_VERIFICATION;
      case ORDER_CREATED -> UserActivity.ActivityType.ORDER_CREATED;
      case ORDER_CANCELLED -> UserActivity.ActivityType.ORDER_CANCELLED;
      case PAYMENT_MADE -> UserActivity.ActivityType.PAYMENT_MADE;
      case PREFERENCE_CHANGED -> UserActivity.ActivityType.PREFERENCE_CHANGED;
      case ACCOUNT_DEACTIVATED -> UserActivity.ActivityType.ACCOUNT_DEACTIVATED;
      case ACCOUNT_ACTIVATED -> UserActivity.ActivityType.ACCOUNT_ACTIVATED;
      case SECURITY_EVENT -> UserActivity.ActivityType.SECURITY_EVENT;
      case SYSTEM_EVENT -> UserActivity.ActivityType.SYSTEM_EVENT;
    };
  }

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
