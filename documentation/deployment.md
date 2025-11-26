# Deployment Guide

## Prerequisites
*   **Docker** & **Docker Compose** (v2+)
*   **Java 17+** (optional, for local jar running)
*   **Maven 3.8+** (optional, for local build)
*   **Node.js 22.12+** (optional, for local frontend dev)

## Environment Configuration

Create a `.env` file in the root directory. You can copy `.env.example` if it exists, or use the following template:

```properties
# Ports
PRODUCT_PORT=8081
ORDER_PORT=8082
COLLECTOR_PORT=8083
DASHBOARD_PORT=8084
ALERT_PORT=8085

# Infrastructure
KAFKA_SERVERS=localhost:9092
MONGO_URI=mongodb://localhost:27017/devpulse_logs
PROMETHEUS_URL=http://localhost:9090

# Frontend (Vite)
VITE_API_PRODUCT_URL=http://localhost:8081
VITE_API_ORDER_URL=http://localhost:8082
VITE_API_LOG_URL=http://localhost:8084
VITE_API_ALERT_URL=http://localhost:8085
VITE_GRAFANA_URL=http://localhost:3000/d/devpulse-dashboard/devpulse-dashboard?theme=dark&kiosk
```

## Running with Docker Compose (Recommended)

To start the entire stack (Infrastructure + Backend + Frontend):

```bash
docker compose up --build -d
```

This will start:
*   Kafka & Zookeeper
*   MongoDB
*   Prometheus & Grafana
*   All Microservices
*   Frontend (served via Nginx or similar if configured in compose, otherwise run locally)

## Running Locally (Hybrid)

You can run the infrastructure in Docker and the services locally for development.

1.  **Start Infrastructure**:
    ```bash
    docker compose up -d kafka zookeeper mongo prometheus grafana
    ```

2.  **Start Backend Services**:
    Use the provided script:
    ```bash
    ./deploy.sh
    ```
    Or run them individually:
    ```bash
    java -jar backend/log-collector/target/log-collector-0.0.1-SNAPSHOT.jar
    # ... repeat for others
    ```

3.  **Start Frontend**:
    ```bash
    cd frontend
    npm install
    npm run dev
    ```

## CI/CD

The project includes a GitHub Actions workflow (`.github/workflows/deploy.yml`) that:
1.  Builds the Backend (Maven).
2.  Builds the Frontend (npm).
3.  Uploads artifacts.
