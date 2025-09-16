package com.flagship.user.repository;

import com.flagship.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * User Repository
 * <p>
 * Data access layer for User entities. Provides custom queries for user management operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByKeycloakId(String keycloakId);

  Optional<User> findByEmail(String email);

  boolean existsByKeycloakId(String keycloakId);

  boolean existsByEmail(String email);

  List<User> findByStatus(User.UserStatus status);

  List<User> findByCreatedAtAfter(LocalDateTime dateTime);

  List<User> findByCity(String city);

  List<User> findByCountry(String country);

  List<User> findByLastLoginBefore(LocalDateTime dateTime);

  @Query("SELECT u FROM User u WHERE " +
      "(:firstName IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND " +
      "(:lastName IS NULL OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%')))")
  List<User> findByNameContaining(@Param("firstName") String firstName,
      @Param("lastName") String lastName);

  @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' AND u.lastLogin > :dateTime")
  List<User> findActiveUsersWithRecentActivity(@Param("dateTime") LocalDateTime dateTime);

  @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
  long countByStatus(@Param("status") User.UserStatus status);

  @Query("SELECT u FROM User u WHERE u.dateOfBirth IS NOT NULL AND " +
      "YEAR(CURRENT_DATE) - YEAR(u.dateOfBirth) BETWEEN :minAge AND :maxAge")
  List<User> findByAgeRange(@Param("minAge") int minAge, @Param("maxAge") int maxAge);
}
