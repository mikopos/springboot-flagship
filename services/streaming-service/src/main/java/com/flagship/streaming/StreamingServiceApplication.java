package com.flagship.streaming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Streaming Service Application
 * <p>
 * This is the main entry point for the Streaming Service that provides: - Real-time event streaming
 * via Server-Sent Events (SSE) - WebFlux-based reactive programming - Kafka event consumption and
 * broadcasting - WebSocket support for bidirectional communication - Event filtering and routing -
 * Client connection management
 *
 * @author Marios Gavriil
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class StreamingServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(StreamingServiceApplication.class, args);
  }
}
