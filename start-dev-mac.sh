#!/bin/bash

# DevPulse Development Launcher (macOS)
# Uses AppleScript to open Terminal tabs

echo "=========================================="
echo "      DevPulse Development Launcher       "
echo "=========================================="

# 1. Build Backend
echo "[1/3] Building backend modules..."
cd backend
mvn clean install -DskipTests
if [ $? -ne 0 ]; then
    echo "Backend build failed. Exiting."
    exit 1
fi
cd ..

# 2. Launch Backend Services
echo "[2/3] Launching backend services..."

# Helper to run command in new tab
run_in_tab() {
    cmd="cd \"$(pwd)/$1\" && $2"
    osascript -e "tell application \"Terminal\" to do script \"$cmd\""
}

run_in_tab "backend/producer-product" "mvn spring-boot:run -DskipTests"
run_in_tab "backend/producer-order" "mvn spring-boot:run -DskipTests"
run_in_tab "backend/log-collector" "mvn spring-boot:run -DskipTests"
run_in_tab "backend/log-dashboard" "mvn spring-boot:run -DskipTests"
run_in_tab "backend/alert-processor" "mvn spring-boot:run -DskipTests"

# 3. Launch Frontend
echo "[3/3] Waiting 15s for backend to initialize..."
sleep 15
echo "Launching Frontend..."

run_in_tab "frontend" "npm install && npm run dev"

echo "=========================================="
echo "All services launched in Terminal tabs!"
echo "=========================================="
