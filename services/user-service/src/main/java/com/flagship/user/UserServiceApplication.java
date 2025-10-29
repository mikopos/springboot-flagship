package com.flagship.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * User Service Application
 * <p>
 * This is the main entry point for the User Service that provides: - User profile management -
 * Integration with Keycloak for authentication - User preference management - User activity
 * tracking - Event publishing for user-related changes
 *
 * @author Marios Gavriil
 * @version 1.0.0
 */
@SpringBootApplication
@EnableKafka
public class UserServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(UserServiceApplication.class, args);
  }
}
