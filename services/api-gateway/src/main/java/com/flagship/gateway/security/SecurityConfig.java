package com.flagship.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;

/**
 * Security Configuration for API Gateway
 * <p>
 * Configures JWT-based authentication and authorization for the gateway. This includes: - JWT token
 * validation - Public endpoints that don't require authentication - Custom authentication entry
 * point - CORS configuration
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

  private static final String JWT_ISSUER_URI = "http://localhost:8080/realms/flagship";

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    return http
        .csrf().disable()
        .cors().and()
        .authorizeExchange(exchanges -> exchanges
            // Public endpoints
            .pathMatchers(
                "/actuator/health",
                "/actuator/info",
                "/api/auth/**",
                "/api/public/**",
                "/fallback/**"
            ).permitAll()
            // All other endpoints require authentication
            .anyExchange().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt
                .jwtDecoder(jwtDecoder())
                .jwtAuthenticationConverter(new JwtAuthenticationConverter())
            )
        )
        .exceptionHandling(exceptions -> exceptions
            .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
        )
        .build();
  }

  @Bean
  public ReactiveJwtDecoder jwtDecoder() {
    NimbusReactiveJwtDecoder jwtDecoder = NimbusReactiveJwtDecoder
        .withJwkSetUri(JWT_ISSUER_URI + "/protocol/openid-connect/certs")
        .build();

    // Add issuer validation
    OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(
        JWT_ISSUER_URI);
    jwtDecoder.setJwtValidator(issuerValidator);

    return jwtDecoder;
  }

}
