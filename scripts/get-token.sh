#!/bin/bash

# Script to get JWT token from Keycloak
# Usage: ./scripts/get-token.sh [username] [password]

KEYCLOAK_URL="http://localhost:8080"
REALM="flagship"
CLIENT_ID="flagship-client"

# Default credentials (change these)
USERNAME=${1:-"testuser"}
PASSWORD=${2:-"testpass"}

echo "Getting JWT token from Keycloak..."
echo "Username: $USERNAME"
echo "Keycloak URL: $KEYCLOAK_URL"

# Check if Keycloak is running
if ! curl -s "$KEYCLOAK_URL/realms/$REALM" > /dev/null; then
    echo "Keycloak is not running. Please start it with: docker-compose up -d keycloak"
    exit 1
fi

# Get the token
echo "Requesting token..."
RESPONSE=$(curl -s -X POST "$KEYCLOAK_URL/realms/$REALM/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=$USERNAME" \
  -d "password=$PASSWORD" \
  -d "grant_type=password" \
  -d "client_id=$CLIENT_ID")

# Check if we got a valid response
if echo "$RESPONSE" | grep -q "access_token"; then
    TOKEN=$(echo "$RESPONSE" | jq -r '.access_token')
    EXPIRES_IN=$(echo "$RESPONSE" | jq -r '.expires_in')
    
    echo "Token obtained successfully!"
    echo "Expires in: $EXPIRES_IN seconds"
    echo ""
    echo "Your JWT Token:"
    echo "$TOKEN"
    echo ""
    echo "Use it in your API calls:"
    echo "curl -H \"Authorization: Bearer $TOKEN\" http://localhost:8081/user-service/users"
    echo ""
    echo "Save to environment variable:"
    echo "export JWT_TOKEN=\"$TOKEN\""
    
    # Save to file for easy access
    echo "$TOKEN" > .jwt-token
    echo "Token saved to .jwt-token file"
    
else
    echo "Failed to get token. Response:"
    echo "$RESPONSE"
    echo ""
    echo "Troubleshooting:"
    echo "1. Make sure Keycloak is running: docker-compose ps keycloak"
    echo "2. Check if user exists in Keycloak admin console"
    echo "3. Verify username and password are correct"
    echo "4. Make sure client 'flagship-client' exists"
fi

