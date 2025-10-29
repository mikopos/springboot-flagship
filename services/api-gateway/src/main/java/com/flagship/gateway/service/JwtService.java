package com.flagship.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * JWT Service for Token Validation and User Information Extraction
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final ReactiveJwtDecoder jwtDecoder;

    /**
     * Validates and decodes a JWT token
     *
     * @param token The JWT token string
     * @return Mono containing the decoded JWT or empty if invalid
     */
    public Mono<Jwt> validateAndDecodeToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.debug("Empty or null token provided");
            return Mono.empty();
        }

        String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;

        return jwtDecoder.decode(cleanToken)
            .doOnNext(jwt -> log.debug("Successfully decoded JWT token for subject: {}", jwt.getSubject()))
            .onErrorResume(JwtException.class, e -> {
                log.warn("JWT validation failed: {}", e.getMessage());
                return Mono.empty();
            })
            .onErrorResume(Exception.class, e -> {
                log.error("Unexpected error during JWT validation", e);
                return Mono.empty();
            });
    }

    /**
     * Extracts user ID from JWT token
     *
     * @param token The JWT token string
     * @return Mono containing the user ID or empty if not found/invalid
     */
    public Mono<String> extractUserId(String token) {
        return validateAndDecodeToken(token)
                .map(this::extractUserIdFromJwt);
    }

    /**
     * Extracts user ID from decoded JWT
     *
     * @param jwt The decoded JWT
     * @return User ID or null if not found
     */
    public String extractUserIdFromJwt(Jwt jwt) {
        if (jwt == null) {
            return null;
        }

        String userId = getClaimAsString(jwt, "sub")
                .orElseGet(() -> getClaimAsString(jwt, "user_id")
                        .orElseGet(() -> getClaimAsString(jwt, "userId")
                                .orElseGet(() -> getClaimAsString(jwt, "preferred_username")
                                        .orElse(null))));

        if (userId != null) {
            log.debug("Extracted user ID: {} from JWT", userId);
        } else {
            log.debug("No user ID found in JWT claims");
        }

        return userId;
    }

    /**
     * Extracts username from JWT token
     *
     * @param token The JWT token string
     * @return Mono containing the username or empty if not found/invalid
     */
    public Mono<String> extractUsername(String token) {
        return validateAndDecodeToken(token)
                .map(this::extractUsernameFromJwt);
    }

    /**
     * Extracts username from decoded JWT
     *
     * @param jwt The decoded JWT
     * @return Username or null if not found
     */
    public String extractUsernameFromJwt(Jwt jwt) {
        if (jwt == null) {
            return null;
        }

        String username = getClaimAsString(jwt, "preferred_username")
                .orElseGet(() -> getClaimAsString(jwt, "username")
                        .orElseGet(() -> getClaimAsString(jwt, "name")
                                .orElseGet(() -> getClaimAsString(jwt, "sub")
                                        .orElse(null))));

        if (username != null) {
            log.debug("Extracted username: {} from JWT", username);
        } else {
            log.debug("No username found in JWT claims");
        }

        return username;
    }

    /**
     * Extracts email from JWT token
     *
     * @param token The JWT token string
     * @return Mono containing the email or empty if not found/invalid
     */
    public Mono<String> extractEmail(String token) {
        return validateAndDecodeToken(token)
                .map(this::extractEmailFromJwt);
    }

    /**
     * Extracts email from decoded JWT
     *
     * @param jwt The decoded JWT
     * @return Email or null if not found
     */
    public String extractEmailFromJwt(Jwt jwt) {
        if (jwt == null) {
            return null;
        }

        String email = getClaimAsString(jwt, "email").orElse(null);
        
        if (email != null) {
            log.debug("Extracted email: {} from JWT", email);
        } else {
            log.debug("No email found in JWT claims");
        }

        return email;
    }

    /**
     * Checks if JWT token is expired
     *
     * @param jwt The decoded JWT
     * @return true if expired, false otherwise
     */
    public boolean isTokenExpired(Jwt jwt) {
        if (jwt == null) {
            return true;
        }

        Instant now = Instant.now();
        Instant expiresAt = jwt.getExpiresAt();
        
        boolean expired = expiresAt != null && now.isAfter(expiresAt);
        
        if (expired) {
            log.debug("JWT token is expired. Expires at: {}, Current time: {}", expiresAt, now);
        }
        
        return expired;
    }

    /**
     * Gets all claims from JWT as a map
     *
     * @param jwt The decoded JWT
     * @return Map of all claims
     */
    public Map<String, Object> getAllClaims(Jwt jwt) {
        if (jwt == null) {
            return Map.of();
        }
        return jwt.getClaims();
    }

    /**
     * Gets a specific claim as String
     *
     * @param jwt The decoded JWT
     * @param claimName The claim name
     * @return Optional containing the claim value or empty if not found
     */
    private Optional<String> getClaimAsString(Jwt jwt, String claimName) {
        Object claim = jwt.getClaim(claimName);
        if (claim instanceof String) {
            return Optional.of((String) claim);
        }
        return Optional.empty();
    }

    /**
     * Gets a specific claim as Object
     *
     * @param jwt The decoded JWT
     * @param claimName The claim name
     * @return Optional containing the claim value or empty if not found
     */
    public Optional<Object> getClaim(Jwt jwt, String claimName) {
        if (jwt == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(jwt.getClaim(claimName));
    }
}
