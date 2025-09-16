package com.flagship.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Payment Service Application
 * <p>
 * This is the main entry point for the Payment Service that provides: - Payment processing and
 * management - Idempotency for payment operations - Resilience patterns with circuit breakers -
 * Integration with external payment providers - Payment event publishing - Fraud detection and risk
 * management
 *
 * @author Marios Gavriil
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
public class PaymentServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(PaymentServiceApplication.class, args);
  }
}
