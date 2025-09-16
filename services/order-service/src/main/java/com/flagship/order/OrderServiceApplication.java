package com.flagship.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Order Service Application
 * <p>
 * This is the main entry point for the Order Service that provides: - Order creation and management
 * - Order status tracking - Domain event publishing - Integration with payment and inventory
 * services - Order history and analytics
 *
 * @author Marios Gavriil
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
public class OrderServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(OrderServiceApplication.class, args);
  }
}
