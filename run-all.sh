#!/bin/bash

echo "🚀 Starting ShopFlow Microservices..."

# Function to open a new terminal tab and run maven
start_service() {
    local service_name=$1
    local port=$2
    echo "   - Starting $service_name on port $port..."
    
    # This command tells MacOS Terminal to open a new window/tab and run the command
    osascript -e "tell application \"Terminal\" to do script \"cd $(pwd)/$service_name; mvn spring-boot:run\""
}

# 1. Start Config Server (Needs to be first!)
start_service "config-server" "8888"
echo "⏳ Waiting 15s for Config Server to initialize..."
sleep 15

# 2. Start Discovery Service
start_service "discovery-service" "8761"
echo "⏳ Waiting 10s for Discovery Service to initialize..."
sleep 10

# 3. Start the Infrastructure & Services
start_service "product-service" "8081"
start_service "order-service" "8082"
start_service "api-gateway" "8080"

echo "✅ All commands sent! Check the opened Terminal windows for logs."