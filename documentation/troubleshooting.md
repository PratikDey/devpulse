# Troubleshooting Guide

## Grafana Shows "No Data"

**Symptom**: You open the Grafana dashboard, but some panels (like "Producer Publish Rate") show "No Data".

**Cause**:
Spring Boot / Micrometer metrics for Kafka are often **lazy-loaded**. They do not exist until the first event occurs. If the application has just started and no logs have been sent, Prometheus cannot scrape the metric because it doesn't exist yet.

**Solution**:
1.  **Generate Traffic**: Go to the "Products" or "Orders" page in the frontend and click "Generate Logs".
2.  Wait 10-15 seconds for Prometheus to scrape the new data.
3.  Refresh Grafana.

## Live Stream Not Working

**Symptom**: The "Live Stream" page is empty, even though logs are being generated.

**Checklist**:
1.  **Log Collector**: Is `log-collector` running? Check logs for "Pushed to dashboard".
2.  **Log Dashboard**: Is `log-dashboard` running?
3.  **Connection**: Check the browser console. Do you see "Connected to SSE"?
    *   If you see 404 or Connection Refused, check `VITE_API_LOG_URL`.
    *   Ensure `log-collector` can reach `log-dashboard` (check `app.dashboard-url` in `application.yaml`).

## Alerts Not Showing

**Symptom**: You sent a critical log, but no toast appeared.

**Checklist**:
1.  **WebSocket**: Check browser console for "Connected to Alert WebSocket".
    *   If failed, check `VITE_API_ALERT_URL`.
    *   Ensure `vite.config.js` has the proxy rule for `/alert-ws`.
2.  **Kafka**: Is `alert-processor` consuming messages? Check its logs.
3.  **Rule**: Does the log match an alert rule? (Default rules: "NullPointerException", "ERROR", "CRITICAL").
