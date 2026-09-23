package com.flagship.gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
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
import org.springframework.security.web.server.csrf.CsrfWebFilter;
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher.MatchResult;

/**
 * Security Configuration for API Gateway
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

  @Value("${jwt.issuer-uri:http://localhost:8080/realms/flagship}")
  private String jwtIssuerUri;

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    return http
        .csrf(csrf -> csrf.requireCsrfProtectionMatcher(csrfProtectionMatcher()))
        .cors(cors -> {})
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


  private static ServerWebExchangeMatcher csrfProtectionMatcher() {
    ServerWebExchangeMatcher bearerToken = exchange -> {
      String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
      return authorization != null && authorization.regionMatches(true, 0, "Bearer ", 0, 7)
          ? MatchResult.match()
          : MatchResult.notMatch();
    };
    return new AndServerWebExchangeMatcher(
        CsrfWebFilter.DEFAULT_CSRF_MATCHER,
        new NegatedServerWebExchangeMatcher(bearerToken));
  }

  @Bean
  public ReactiveJwtDecoder jwtDecoder() {
    NimbusReactiveJwtDecoder jwtDecoder = NimbusReactiveJwtDecoder
        .withJwkSetUri(jwtIssuerUri + "/protocol/openid-connect/certs")
        .build();

    OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(
        jwtIssuerUri);
    jwtDecoder.setJwtValidator(issuerValidator);

    return jwtDecoder;
  }

}
