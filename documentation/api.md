# API Documentation

## Base URLs
*   **Logs**: `http://localhost:8084` (or configured via `VITE_API_LOG_URL`)
*   **Alerts**: `http://localhost:8085` (or configured via `VITE_API_ALERT_URL`)
*   **Products**: `http://localhost:8081` (or configured via `VITE_API_PRODUCT_URL`)
*   **Orders**: `http://localhost:8082` (or configured via `VITE_API_ORDER_URL`)

---

## Logs API (`log-dashboard`)

### Get All Logs
`GET /api/logs`
*   **Params**: `page` (int), `size` (int)
*   **Response**: Paginated list of logs.

### Get Recent Logs
`GET /api/logs/recent`
*   **Response**: Top 100 most recent logs.

### Get Logs by Service
`GET /api/logs/service/{serviceName}`

### Get Logs by Level
`GET /api/logs/level/{level}`
*   **Level**: `INFO`, `WARN`, `ERROR`, `DEBUG`.

### Live Stream (SSE)
`GET /api/logs/stream`
*   **Type**: `text/event-stream`
*   **Events**: `init`, `log`.

---

## Alerts API (`alert-processor`)

### Get All Alerts
`GET /api/alerts`
*   **Response**: List of historical alerts sorted by timestamp (descending).

### WebSocket
*   **Endpoint**: `/alert-ws`
*   **Topic**: `/topic/alerts`
*   **Payload**: JSON object with alert details.

---

## Invalid Logs API (`log-collector`)

### Get Invalid Logs
`GET /api/logs/invalid`
*   **Response**: List of malformed log messages that failed parsing.

---

## Product API (`producer-product`)

### Get Products
`GET /api/products`

### Create Product
`POST /api/products`

### Generate Dummy Traffic
`POST /api/products/generate`
*   **Params**: `count` (int) - Number of logs to generate.

---

## Order API (`producer-order`)

### Get Orders
`GET /api/orders`

### Create Order
`POST /api/orders`

### Generate Dummy Traffic
`POST /api/orders/generate`
*   **Params**: `count` (int) - Number of logs to generate.
