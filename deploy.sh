#!/bin/bash

# Deployment Script for DevPulse
# Usage: ./deploy.sh

# Load environment variables
if [ -f .env ]; then
    set -a
    source .env
    set +a
else
    echo "Error: .env file not found!"
    exit 1
fi

echo "Updating Nginx configuration..."
if [ -f infra/nginx/devpulse ]; then
    sudo cp infra/nginx/devpulse /etc/nginx/sites-available/devpulse
    sudo ln -sf /etc/nginx/sites-available/devpulse /etc/nginx/sites-enabled/
    sudo nginx -t && sudo systemctl reload nginx
    echo "Nginx configuration updated and reloaded."
else
    echo "Warning: infra/nginx/devpulse not found. Skipping Nginx update."
fi

echo "Deploying Frontend..."
if [ -d "frontend/dist" ]; then
    sudo mkdir -p /var/www/devpulse
    sudo cp -r frontend/dist/* /var/www/devpulse/
    echo "Frontend deployed to /var/www/devpulse"
else
    echo "Warning: frontend/dist not found. Skipping frontend deployment."
fi

echo "Deploying Backend Services..."

# Create deployment directory
sudo mkdir -p /root/deploy/devpulse/app

# Copy jars
echo "Copying jars..."
sudo cp backend/producer-product/target/producer-product-*.jar /root/deploy/devpulse/app/producer-product.jar
sudo cp backend/producer-order/target/producer-order-*.jar /root/deploy/devpulse/app/producer-order.jar
sudo cp backend/log-collector/target/log-collector-*.jar /root/deploy/devpulse/app/log-collector.jar
sudo cp backend/log-dashboard/target/log-dashboard-*.jar /root/deploy/devpulse/app/log-dashboard.jar
sudo cp backend/alert-processor/target/alert-processor-*.jar /root/deploy/devpulse/app/alert-processor.jar

# Reload systemd
sudo systemctl daemon-reload

# Restart services
echo "Restarting services..."
sudo systemctl restart producer-product producer-order log-collector log-dashboard alert-processor

echo "All services restarted via systemd."