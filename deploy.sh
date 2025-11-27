#!/bin/bash

# Deployment Script for DevPulse
# Usage: ./deploy.sh

# Load environment variables
if [ -f .env ]; then
    export $(cat .env | xargs)
else
    echo "Error: .env file not found!"
    exit 1
fi

echo "Starting DevPulse Services..."

# Start Producer Product
nohup java -jar backend/producer-product/target/producer-product-*.jar --spring.profiles.active=prod > logs/producer-product.log 2>&1 &
echo "Started Producer Product"

# Start Producer Order
nohup java -jar backend/producer-order/target/producer-order-*.jar --spring.profiles.active=prod > logs/producer-order.log 2>&1 &
echo "Started Producer Order"

# Start Log Collector
nohup java -jar backend/log-collector/target/log-collector-*.jar --spring.profiles.active=prod > logs/log-collector.log 2>&1 &
echo "Started Log Collector"

# Start Log Dashboard
nohup java -jar backend/log-dashboard/target/log-dashboard-*.jar --spring.profiles.active=prod > logs/log-dashboard.log 2>&1 &
echo "Started Log Dashboard"

# Start Alert Processor
nohup java -jar backend/alert-processor/target/alert-processor-*.jar --spring.profiles.active=prod > logs/alert-processor.log 2>&1 &
echo "Started Alert Processor"

echo "All services started. Check logs/ directory for output."
