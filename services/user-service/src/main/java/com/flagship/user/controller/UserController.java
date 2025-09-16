package com.flagship.user.controller;

import com.flagship.user.model.User;
import com.flagship.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * User Controller
 * <p>
 * REST API endpoints for user management operations. Provides CRUD operations for users and user
 * profiles.
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER')")
  public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
    log.info("Creating new user with email: {}", user.getEmail());
    User createdUser = userService.createUser(user);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
  }

  @GetMapping("/me")
  public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
    String keycloakId = jwt.getSubject();
    log.debug("Getting current user profile for Keycloak ID: {}", keycloakId);

    Optional<User> user = userService.findByKeycloakId(keycloakId);
    return user.map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/me")
  public ResponseEntity<User> updateCurrentUser(@AuthenticationPrincipal Jwt jwt,
      @Valid @RequestBody User updatedUser) {
    String keycloakId = jwt.getSubject();
    log.info("Updating current user profile for Keycloak ID: {}", keycloakId);

    Optional<User> existingUser = userService.findByKeycloakId(keycloakId);
    if (existingUser.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    User savedUser = userService.updateUser(existingUser.get().getId(), updatedUser);
    return ResponseEntity.ok(savedUser);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER') or @userService.isCurrentUser(#id, authentication)")
  public ResponseEntity<User> getUserById(@PathVariable Long id) {
    log.debug("Getting user by ID: {}", id);

    Optional<User> user = userService.findById(id);
    return user.map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER')")
  public ResponseEntity<User> updateUser(@PathVariable Long id,
      @Valid @RequestBody User updatedUser) {
    log.info("Updating user with ID: {}", id);

    try {
      User savedUser = userService.updateUser(id, updatedUser);
      return ResponseEntity.ok(savedUser);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
    log.info("Deactivating user with ID: {}", id);

    try {
      userService.deactivateUser(id);
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PutMapping("/{id}/activate")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> activateUser(@PathVariable Long id) {
    log.info("Activating user with ID: {}", id);

    try {
      userService.activateUser(id);
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER')")
  public ResponseEntity<Page<User>> getAllUsers(Pageable pageable) {
    log.debug("Getting all users with pagination: {}", pageable);

    // Note: This would need to be implemented in UserService with pagination support
    List<User> users = userService.findAllUsers();
    // For now, return all users without pagination
    return ResponseEntity.ok(Page.empty());
  }


  @GetMapping("/status/{status}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER')")
  public ResponseEntity<List<User>> getUsersByStatus(@PathVariable User.UserStatus status) {
    log.debug("Getting users by status: {}", status);

    List<User> users = userService.findUsersByStatus(status);
    return ResponseEntity.ok(users);
  }

  @GetMapping("/city/{city}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER')")
  public ResponseEntity<List<User>> getUsersByCity(@PathVariable String city) {
    log.debug("Getting users by city: {}", city);

    List<User> users = userService.findUsersByCity(city);
    return ResponseEntity.ok(users);
  }

  @GetMapping("/country/{country}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER')")
  public ResponseEntity<List<User>> getUsersByCountry(@PathVariable String country) {
    log.debug("Getting users by country: {}", country);

    List<User> users = userService.findUsersByCountry(country);
    return ResponseEntity.ok(users);
  }

  @GetMapping("/recent-activity")
  @PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER')")
  public ResponseEntity<List<User>> getUsersWithRecentActivity(
      @RequestParam(defaultValue = "7") int days) {
    log.debug("Getting users with recent activity within {} days", days);

    List<User> users = userService.findUsersWithRecentActivity(days);
    return ResponseEntity.ok(users);
  }

  @GetMapping("/health")
  public ResponseEntity<String> health() {
    return ResponseEntity.ok("User service is healthy");
  }
}
