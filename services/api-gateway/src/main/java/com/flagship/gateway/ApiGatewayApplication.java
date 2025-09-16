package com.flagship.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway Application
 * <p>
 * This is the main entry point for the API Gateway service that provides: - Request routing to
 * backend microservices - Authentication and authorization - Rate limiting and throttling - Load
 * balancing - Circuit breaker patterns - Request/response logging and monitoring
 *
 * @author Marios Gavriil
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

  public static void main(String[] args) {
    SpringApplication.run(ApiGatewayApplication.class, args);
  }
}
