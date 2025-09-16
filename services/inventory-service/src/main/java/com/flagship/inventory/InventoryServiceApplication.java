package com.flagship.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Inventory Service Application
 * <p>
 * This is the main entry point for the Inventory Service that provides: - Product inventory
 * management - Stock level tracking and updates - Redis caching for high-performance access -
 * Integration with order and payment services - Inventory event publishing - Low stock alerts and
 * notifications
 *
 * @author Marios Gavriil
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
@EnableCaching
public class InventoryServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(InventoryServiceApplication.class, args);
  }
}
