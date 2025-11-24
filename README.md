# DevPulse

DevPulse — a lightweight open-source developer observability platform (logs + metrics + alerts).

This repository contains a monorepo for an end-to-end demo:
- Producers (dummy services) that generate logs
- Kafka as the streaming bus
- Log Collector (consumes logs and stores in MongoDB)
- Log Dashboard (REST APIs + WebSocket for UI)
- Alert Processor (evaluates alert rules, sends toast + email)
- Metrics pipeline (Spring Actuator → Prometheus → Grafana)
- React frontend (dashboard + charts + toast alerts + Grafana metrics)

> Goal: A small, extensible observability stack you can run locally and extend easily.

---

## Quick links
- `/backend` — all backend modules (common, producers, collector, dashboard, alert-processor)
- `/frontend` — React app
- `/docs` — architecture diagrams, API contracts, alert rules, roadmap
- `docker-compose.yml` — brings up Kafka, Mongo, Prometheus, Grafana, and services

---

## Prerequisites (local dev)
- Docker & Docker Compose (docker-compose v1.29+ or v2)
- Java 17+ & Maven (if you want to run apps locally instead of using Docker)
- Node.js (for frontend dev)

---

## Quick start (one-liner)
From repo root:
```bash
# builds images for services (uses Dockerfiles in module folders)
docker-compose up --build
