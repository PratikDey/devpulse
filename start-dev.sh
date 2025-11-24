#!/bin/bash

# DevPulse Development Startup Script
# Launches all backend services and frontend in separate gnome-terminal tabs.

echo "=========================================="
echo "      DevPulse Development Launcher       "
echo "=========================================="

# 1. Build Backend
echo "[1/3] Building backend modules (skipping tests for speed)..."
cd backend
mvn clean install -DskipTests
if [ $? -ne 0 ]; then
    echo "Backend build failed. Exiting."
    exit 1
fi
cd ..

# 2. Launch Backend Services
echo "[2/3] Launching backend services in new terminal tabs..."

# Function to launch a tab
launch_tab() {
    title=$1
    cmd=$2
    gnome-terminal --tab --title="$title" -- bash -c "$cmd; exec bash"
}

# We use a single gnome-terminal window with multiple tabs
gnome-terminal --tab --title="Producer Product (8081)" -- bash -c "cd backend/producer-product; mvn spring-boot:run -DskipTests; exec bash"
gnome-terminal --tab --title="Producer Order (8082)" -- bash -c "cd backend/producer-order; mvn spring-boot:run -DskipTests; exec bash"
gnome-terminal --tab --title="Log Collector (8083)" -- bash -c "cd backend/log-collector; mvn spring-boot:run -DskipTests; exec bash"
gnome-terminal --tab --title="Log Dashboard (8084)" -- bash -c "cd backend/log-dashboard; mvn spring-boot:run -DskipTests; exec bash"
gnome-terminal --tab --title="Alert Processor (8085)" -- bash -c "cd backend/alert-processor; mvn spring-boot:run -DskipTests; exec bash"

# 3. Launch Frontend
echo "[3/3] Launching Frontend..."
gnome-terminal --tab --title="Frontend (Vite)" -- bash -c "cd frontend; echo 'Installing dependencies...'; npm install; echo 'Starting Vite...'; npm run dev; exec bash"

echo "=========================================="
echo "All services launched!"
echo "Backend services are starting in separate tabs."
echo "Frontend is starting in a separate tab."
echo "=========================================="
