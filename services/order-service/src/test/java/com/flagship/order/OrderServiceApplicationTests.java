package com.flagship.order;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Order Service Application Tests
 * <p>
 * Basic integration tests for the Order Service application. Tests application context loading and
 * basic functionality.
 */
@SpringBootTest
@ActiveProfiles("test")
class OrderServiceApplicationTests {

  @Test
  void contextLoads() {
    // This test ensures that the Spring application context loads successfully
    // If there are any configuration issues, this test will fail
  }
}
