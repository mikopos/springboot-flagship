package com.flagship.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * User Entity
 * <p>
 * Represents a user in the system with profile information. This entity stores user data that
 * complements the authentication information stored in Keycloak.
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_keycloak_id", columnList = "keycloakId"),
    @Index(name = "idx_user_email", columnList = "email")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "keycloak_id", unique = true, nullable = false)
  @NotBlank(message = "Keycloak ID is required")
  private String keycloakId;

  @Column(name = "email", unique = true, nullable = false)
  @Email(message = "Email should be valid")
  @NotBlank(message = "Email is required")
  private String email;

  @Column(name = "first_name")
  @Size(max = 50, message = "First name must not exceed 50 characters")
  private String firstName;

  @Column(name = "last_name")
  @Size(max = 50, message = "Last name must not exceed 50 characters")
  private String lastName;

  @Column(name = "phone_number")
  @Size(max = 20, message = "Phone number must not exceed 20 characters")
  private String phoneNumber;

  @Column(name = "date_of_birth")
  private LocalDateTime dateOfBirth;

  @Enumerated(EnumType.STRING)
  @Column(name = "gender")
  private Gender gender;

  @Column(name = "address")
  @Size(max = 500, message = "Address must not exceed 500 characters")
  private String address;

  @Column(name = "city")
  @Size(max = 100, message = "City must not exceed 100 characters")
  private String city;

  @Column(name = "country")
  @Size(max = 100, message = "Country must not exceed 100 characters")
  private String country;

  @Column(name = "postal_code")
  @Size(max = 20, message = "Postal code must not exceed 20 characters")
  private String postalCode;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  @Builder.Default
  private UserStatus status = UserStatus.ACTIVE;

  @Column(name = "preferences", columnDefinition = "TEXT")
  private String preferences;

  @Column(name = "last_login")
  private LocalDateTime lastLogin;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Builder.Default
  private List<UserActivity> activities = new ArrayList<>();

  public void updateLastLogin() {
    this.lastLogin = LocalDateTime.now();
  }

  public String getFullName() {
    if (firstName != null && lastName != null) {
      return firstName + " " + lastName;
    } else if (firstName != null) {
      return firstName;
    } else if (lastName != null) {
      return lastName;
    }
    return email;
  }

  public boolean isActive() {
    return status == UserStatus.ACTIVE;
  }

  public void deactivate() {
    this.status = UserStatus.INACTIVE;
  }

  public void activate() {
    this.status = UserStatus.ACTIVE;
  }

  public enum Gender {
    MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
  }

  public enum UserStatus {
    ACTIVE, INACTIVE, SUSPENDED, PENDING_VERIFICATION
  }
}
