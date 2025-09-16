package com.flagship.user.service;

import com.flagship.user.event.UserEvent;
import com.flagship.user.model.User;
import com.flagship.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * User Service
 * <p>
 * Business logic layer for user management operations. Handles user CRUD operations, profile
 * management, and event publishing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

  private final UserRepository userRepository;
  private final KafkaTemplate<String, UserEvent> kafkaTemplate;
  private final UserActivityService userActivityService;

  public User createUser(User user) {
    log.info("Creating new user with email: {}", user.getEmail());

    if (userRepository.existsByKeycloakId(user.getKeycloakId())) {
      throw new IllegalArgumentException(
          "User with Keycloak ID already exists: " + user.getKeycloakId());
    }

    if (userRepository.existsByEmail(user.getEmail())) {
      throw new IllegalArgumentException("User with email already exists: " + user.getEmail());
    }

    User savedUser = userRepository.save(user);

    userActivityService.logActivity(savedUser, UserActivityService.ActivityType.PROFILE_UPDATE,
        "User account created", null, null);

    publishUserEvent(savedUser, UserEvent.UserEventType.USER_CREATED);

    log.info("User created successfully with ID: {}", savedUser.getId());
    return savedUser;
  }

  @Transactional(readOnly = true)
  public Optional<User> findById(Long id) {
    return userRepository.findById(id);
  }

  @Transactional(readOnly = true)
  public Optional<User> findByKeycloakId(String keycloakId) {
    return userRepository.findByKeycloakId(keycloakId);
  }

  @Transactional(readOnly = true)
  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  public User updateUser(Long id, User updatedUser) {
    log.info("Updating user with ID: {}", id);

    User existingUser = userRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

    existingUser.setFirstName(updatedUser.getFirstName());
    existingUser.setLastName(updatedUser.getLastName());
    existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
    existingUser.setDateOfBirth(updatedUser.getDateOfBirth());
    existingUser.setGender(updatedUser.getGender());
    existingUser.setAddress(updatedUser.getAddress());
    existingUser.setCity(updatedUser.getCity());
    existingUser.setCountry(updatedUser.getCountry());
    existingUser.setPostalCode(updatedUser.getPostalCode());
    existingUser.setPreferences(updatedUser.getPreferences());

    User savedUser = userRepository.save(existingUser);

    userActivityService.logActivity(savedUser, UserActivityService.ActivityType.PROFILE_UPDATE,
        "User profile updated", null, null);

    publishUserEvent(savedUser, UserEvent.UserEventType.USER_UPDATED);

    log.info("User updated successfully with ID: {}", savedUser.getId());
    return savedUser;
  }

  public void updateLastLogin(String keycloakId) {
    log.debug("Updating last login for user with Keycloak ID: {}", keycloakId);

    Optional<User> userOpt = userRepository.findByKeycloakId(keycloakId);
    if (userOpt.isPresent()) {
      User user = userOpt.get();
      user.updateLastLogin();
      userRepository.save(user);

      userActivityService.logActivity(user, UserActivityService.ActivityType.LOGIN,
          "User logged in", null, null);
    }
  }

  public void deactivateUser(Long id) {
    log.info("Deactivating user with ID: {}", id);

    User user = userRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

    user.deactivate();
    userRepository.save(user);

    userActivityService.logActivity(user, UserActivityService.ActivityType.ACCOUNT_DEACTIVATED,
        "User account deactivated", null, null);

    publishUserEvent(user, UserEvent.UserEventType.USER_DEACTIVATED);

    log.info("User deactivated successfully with ID: {}", user.getId());
  }

  public void activateUser(Long id) {
    log.info("Activating user with ID: {}", id);

    User user = userRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

    user.activate();
    userRepository.save(user);

    userActivityService.logActivity(user, UserActivityService.ActivityType.ACCOUNT_ACTIVATED,
        "User account activated", null, null);

    publishUserEvent(user, UserEvent.UserEventType.USER_ACTIVATED);

    log.info("User activated successfully with ID: {}", user.getId());
  }

  @Transactional(readOnly = true)
  public List<User> findAllUsers() {
    return userRepository.findAll();
  }

  @Transactional(readOnly = true)
  public List<User> findUsersByStatus(User.UserStatus status) {
    return userRepository.findByStatus(status);
  }

  @Transactional(readOnly = true)
  public List<User> findUsersByCity(String city) {
    return userRepository.findByCity(city);
  }

  @Transactional(readOnly = true)
  public List<User> findUsersByCountry(String country) {
    return userRepository.findByCountry(country);
  }

  @Transactional(readOnly = true)
  public List<User> findUsersWithRecentActivity(int days) {
    LocalDateTime threshold = LocalDateTime.now().minusDays(days);
    return userRepository.findActiveUsersWithRecentActivity(threshold);
  }

  private void publishUserEvent(User user, UserEvent.UserEventType eventType) {
    try {
      UserEvent event = UserEvent.builder()
          .userId(user.getId())
          .keycloakId(user.getKeycloakId())
          .email(user.getEmail())
          .eventType(eventType)
          .timestamp(LocalDateTime.now())
          .build();

      kafkaTemplate.send("user-events", event);
      log.debug("Published user event: {} for user: {}", eventType, user.getId());
    } catch (Exception e) {
      log.error("Failed to publish user event: {} for user: {}", eventType, user.getId(), e);
    }
  }
}
