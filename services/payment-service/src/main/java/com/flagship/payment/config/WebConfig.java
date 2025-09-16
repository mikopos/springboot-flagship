package com.flagship.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Web Configuration for Payment Service
 * <p>
 * Configures web-related beans including RestTemplate for external API calls.
 */
@Configuration
public class WebConfig {

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}
