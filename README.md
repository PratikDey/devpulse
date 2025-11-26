# DevPulse

**DevPulse** is a lightweight, open-source developer observability platform. It demonstrates a complete end-to-end stack for logging, monitoring, and alerting using modern technologies.

## 🚀 Features

*   **Live Log Streaming**: Real-time log viewing via Server-Sent Events (SSE).
*   **Distributed Tracing**: Correlate logs across microservices.
*   **Alerting**: Log-based and metrics-based alerting via **Toast Notifications** and **Email**.
*   **Metrics**: Integrated Prometheus & Grafana dashboards.
*   **Microservices**: Spring Boot architecture with Kafka messaging.

## 📚 Documentation

Detailed documentation is available in the `documentation/` folder:

*   [**Architecture**](documentation/architecture.md): System design, services, and data flow.
*   [**Deployment Guide**](documentation/deployment.md): How to run locally or with Docker.
*   [**API Reference**](documentation/api.md): REST API endpoints.
*   [**Troubleshooting**](documentation/troubleshooting.md): Common issues and fixes.

## 🛠️ Tech Stack

*   **Backend**: Java 17, Spring Boot 3.5.7, Spring Cloud 2025.0.0
*   **Frontend**: React, Vite, TailwindCSS
*   **Messaging**: Apache Kafka
*   **Database**: MongoDB
*   **Monitoring**: Prometheus, Grafana

## ⚡ Quick Start

1.  **Start Infrastructure** (Kafka, Mongo, Prometheus, Grafana):
    ```bash
    docker compose up -d
    ```

2.  **Start Services**:
    ```bash
    ./deploy.sh
    ```

3.  **Start Frontend**:
    ```bash
    cd frontend
    npm install
    npm run dev
    ```

4.  **Visit**: `http://localhost:5173`

## 🤝 Contributors

*   **Pratik Dey** - [@PratikDey](https://github.com/PratikDey)
*   **Pratyush Kumar** - [@MightyThinker](https://github.com/MightyThinker)

## 📜 License

MIT
