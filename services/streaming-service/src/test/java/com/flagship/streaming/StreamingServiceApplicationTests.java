package com.flagship.streaming;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Streaming Service Application Tests
 * <p>
 * Basic integration tests for the Streaming Service application. Tests application context loading
 * and basic functionality.
 */
@SpringBootTest
@ActiveProfiles("test")
class StreamingServiceApplicationTests {

  @Test
  void contextLoads() {
    // This test ensures that the Spring application context loads successfully
    // If there are any configuration issues, this test will fail
  }
}
