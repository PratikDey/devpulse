@echo off
echo ==========================================
echo       DevPulse Development Launcher       
echo ==========================================

REM 1. Build Backend
echo [1/3] Building backend modules...
cd backend
call mvn clean install -DskipTests
if %errorlevel% neq 0 (
    echo Backend build failed. Exiting.
    pause
    exit /b %errorlevel%
)
cd ..

REM 2. Launch Backend Services
echo [2/3] Launching backend services...

start "Producer Product" cmd /k "cd backend\producer-product && mvn spring-boot:run -DskipTests"
start "Producer Order" cmd /k "cd backend\producer-order && mvn spring-boot:run -DskipTests"
start "Log Collector" cmd /k "cd backend\log-collector && mvn spring-boot:run -DskipTests"
start "Log Dashboard" cmd /k "cd backend\log-dashboard && mvn spring-boot:run -DskipTests"
start "Alert Processor" cmd /k "cd backend\alert-processor && mvn spring-boot:run -DskipTests"

REM 3. Launch Frontend
echo [3/3] Waiting 15s for backend to initialize...
timeout /t 15

echo Launching Frontend...
start "Frontend" cmd /k "cd frontend && npm install && npm run dev"

echo ==========================================
echo All services launched!
echo ==========================================
pause
