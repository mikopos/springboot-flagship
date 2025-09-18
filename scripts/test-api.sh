#!/bin/bash

# Script to test API endpoints with JWT token
# Usage: ./scripts/test-api.sh [token]

TOKEN=${1:-$(cat .jwt-token 2>/dev/null)}
BASE_URL="http://localhost:8081"

if [ -z "$TOKEN" ]; then
    echo "No JWT token provided. Please run ./scripts/get-token.sh first"
    exit 1
fi

echo "Testing API endpoints with JWT token..."
echo "Base URL: $BASE_URL"
echo ""

# Test health endpoint (no auth required)
echo "1. Testing health endpoint..."
curl -s "$BASE_URL/actuator/health" | jq '.' 2>/dev/null || echo "Health check response received"
echo ""

# Test user service
echo "2. Testing user service..."
curl -s -H "Authorization: Bearer $TOKEN" \
  "$BASE_URL/user-service/users" | jq '.' 2>/dev/null || echo "User service response received"
echo ""

# Test inventory service
echo "3. Testing inventory service..."
curl -s -H "Authorization: Bearer $TOKEN" \
  "$BASE_URL/inventory-service/products" | jq '.' 2>/dev/null || echo "Inventory service response received"
echo ""

# Test order service
echo "4. Testing order service..."
curl -s -H "Authorization: Bearer $TOKEN" \
  "$BASE_URL/order-service/orders" | jq '.' 2>/dev/null || echo "Order service response received"
echo ""

# Test payment service
echo "5. Testing payment service..."
curl -s -H "Authorization: Bearer $TOKEN" \
  "$BASE_URL/payment-service/payments" | jq '.' 2>/dev/null || echo "Payment service response received"
echo ""

echo "API testing completed!"
echo "Tip: Install jq for better JSON formatting: brew install jq"

