#!/bin/bash

echo "Starting Flagship E-Commerce Microservices Suite..."

echo "Starting infrastructure services..."
docker-compose up -d postgres redis kafka zookeeper keycloak prometheus grafana

echo "Waiting for infrastructure services to be ready..."
sleep 30

echo "Starting microservices..."

echo "Starting API Gateway..."
cd services/api-gateway
mvn spring-boot:run -Dspring-boot.run.profiles=docker &
API_GATEWAY_PID=$!
cd ../..

echo "Starting User Service..."
cd services/user-service
mvn spring-boot:run -Dspring-boot.run.profiles=docker &
USER_SERVICE_PID=$!
cd ../..

echo "Starting Order Service..."
cd services/order-service
mvn spring-boot:run -Dspring-boot.run.profiles=docker &
ORDER_SERVICE_PID=$!
cd ../..

echo "Starting Payment Service..."
cd services/payment-service
mvn spring-boot:run -Dspring-boot.run.profiles=docker &
PAYMENT_SERVICE_PID=$!
cd ../..

echo "Starting Inventory Service..."
cd services/inventory-service
mvn spring-boot:run -Dspring-boot.run.profiles=docker &
INVENTORY_SERVICE_PID=$!
cd ../..

echo "Starting Streaming Service..."
cd services/streaming-service
mvn spring-boot:run -Dspring-boot.run.profiles=docker &
STREAMING_SERVICE_PID=$!
cd ../..

echo "All services started!"
echo "API Gateway PID: $API_GATEWAY_PID"
echo "User Service PID: $USER_SERVICE_PID"
echo "Order Service PID: $ORDER_SERVICE_PID"
echo "Payment Service PID: $PAYMENT_SERVICE_PID"
echo "Inventory Service PID: $INVENTORY_SERVICE_PID"
echo "Streaming Service PID: $STREAMING_SERVICE_PID"

echo ""
echo "Services are running on:"
echo "API Gateway: http://localhost:8081"
echo "User Service: http://localhost:8082"
echo "Order Service: http://localhost:8083"
echo "Payment Service: http://localhost:8084"
echo "Inventory Service: http://localhost:8085"
echo "Streaming Service: http://localhost:8086"
echo "Prometheus: http://localhost:9090"
echo "Grafana: http://localhost:3000"
echo "Keycloak: http://localhost:8080"

echo ""
echo "Press Ctrl+C to stop all services"

# Wait for user to stop
wait
