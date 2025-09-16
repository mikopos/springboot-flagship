package com.flagship.inventory;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Inventory Service Application Tests
 * 
 * Basic integration tests for the Inventory Service application.
 * Tests application context loading and basic functionality.
 */
@SpringBootTest
@ActiveProfiles("test")
class InventoryServiceApplicationTests {

    @Test
    void contextLoads() {
        // This test ensures that the Spring application context loads successfully
        // If there are any configuration issues, this test will fail
    }
}
