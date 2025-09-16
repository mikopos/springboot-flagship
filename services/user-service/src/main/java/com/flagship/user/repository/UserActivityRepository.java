package com.flagship.user.repository;

import com.flagship.user.model.UserActivity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User Activity Repository
 * <p>
 * Data access layer for UserActivity entities. Provides custom queries for user activity tracking
 * and analytics.
 */
@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {

  List<UserActivity> findByUserIdOrderByTimestampDesc(Long userId);

  List<UserActivity> findByUserIdAndActivityTypeOrderByTimestampDesc(Long userId,
      UserActivity.ActivityType activityType);

  List<UserActivity> findByUserIdAndTimestampBetweenOrderByTimestampDesc(Long userId,
      LocalDateTime startDate, LocalDateTime endDate);

  List<UserActivity> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);

  List<UserActivity> findByActivityTypeOrderByTimestampDesc(UserActivity.ActivityType activityType);

  List<UserActivity> findByIpAddressOrderByTimestampDesc(String ipAddress);

  List<UserActivity> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime startDate,
      LocalDateTime endDate);

  @Query("SELECT ua FROM UserActivity ua WHERE ua.user.id = :userId ORDER BY ua.timestamp DESC")
  List<UserActivity> findTopByUserIdOrderByTimestampDesc(@Param("userId") Long userId,
      @Param("limit") int limit);

  @Query("SELECT COUNT(ua) FROM UserActivity ua WHERE ua.user.id = :userId AND ua.activityType = :activityType")
  long countByUserIdAndActivityType(@Param("userId") Long userId,
      @Param("activityType") UserActivity.ActivityType activityType);

  @Query("SELECT ua FROM UserActivity ua WHERE ua.user.id = :userId AND ua.metadata IS NOT NULL ORDER BY ua.timestamp DESC")
  List<UserActivity> findByUserIdWithMetadata(@Param("userId") Long userId);

  @Query("SELECT ua FROM UserActivity ua WHERE ua.user.id = :userId AND LOWER(ua.description) LIKE LOWER(CONCAT('%', :descriptionPattern, '%')) ORDER BY ua.timestamp DESC")
  List<UserActivity> findByUserIdAndDescriptionContaining(@Param("userId") Long userId,
      @Param("descriptionPattern") String descriptionPattern);

  @Query("SELECT ua FROM UserActivity ua WHERE ua.user.id = :userId AND ua.ipAddress = :ipAddress ORDER BY ua.timestamp DESC")
  List<UserActivity> findByUserIdAndIpAddress(@Param("userId") Long userId,
      @Param("ipAddress") String ipAddress);
}
