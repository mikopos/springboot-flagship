package com.flagship.gateway.security;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * JWT Authentication Converter
 * <p>
 * Converts JWT tokens to Spring Security authentication objects. Extracts authorities from JWT
 * claims and creates proper authentication tokens.
 */
@Component
public class JwtAuthenticationConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

  private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

  @Override
  public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
    Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
    return Mono.just(new JwtAuthenticationToken(jwt, authorities));
  }

  private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    Collection<GrantedAuthority> authorities = jwtGrantedAuthoritiesConverter.convert(jwt);
    Collection<GrantedAuthority> keycloakAuthorities = extractKeycloakAuthorities(jwt);
    authorities.addAll(keycloakAuthorities);

    return authorities;
  }

  @SuppressWarnings("unchecked")
  private Collection<GrantedAuthority> extractKeycloakAuthorities(Jwt jwt) {
    Object realmAccess = jwt.getClaim("realm_access");
    if (realmAccess instanceof java.util.Map) {
      java.util.Map<String, Object> realmAccessMap = (java.util.Map<String, Object>) realmAccess;
      Object roles = realmAccessMap.get("roles");
      if (roles instanceof List) {
        return ((List<String>) roles).stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
            .collect(Collectors.toList());
      }
    }

    Object resourceAccess = jwt.getClaim("resource_access");
    if (resourceAccess instanceof java.util.Map) {
      java.util.Map<String, Object> resourceAccessMap = (java.util.Map<String, Object>) resourceAccess;
      Object clientAccess = resourceAccessMap.get("flagship-client");
      if (clientAccess instanceof java.util.Map) {
        java.util.Map<String, Object> clientAccessMap = (java.util.Map<String, Object>) clientAccess;
        Object roles = clientAccessMap.get("roles");
        if (roles instanceof List) {
          return ((List<String>) roles).stream()
              .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
              .collect(Collectors.toList());
        }
      }
    }

    return Collections.emptyList();
  }
}
