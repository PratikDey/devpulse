package com.devpulse.alertprocessor.metrics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrometheusClient {

    private final ObjectMapper mapper;
    private final WebClient webClient;

    /**
     * Execute a synchronous "instant" Prometheus query and return the numeric value of the first result.
     *
     * Behavior:
     *  - Builds a URL: {baseUrl}/api/v1/query?query={URLEncoded(promql)}.
     *  - Uses the injected WebClient and blocks on the response (returns null on timeout/error).
     *  - Expects Prometheus JSON shape: { "status": "success", "data": { "result": [ { "value": [ <ts>, "<value>" ] }, ... ] } }.
     *  - Returns the parsed double value from the first result element, or null if no result / non-success / parse error.
     *
     * Caveats:
     *  - Uses blocking .block() on a reactive WebClient. If high throughput is expected, switch to non-blocking/reactive handling
     *    or run calls in a bounded thread pool.
     *
     * @param baseUrl base Prometheus URL (e.g. http://localhost:9090)
     * @param promql  PromQL expression to execute
     * @return parsed numeric value of the first result, or null if unavailable / on error
     */

    public Double queryInstant(String baseUrl, String promql) {
        try {
            String encoded = URLEncoder.encode(promql, StandardCharsets.UTF_8);
            String url = baseUrl.endsWith("/") ? baseUrl + "api/v1/query?query=" + encoded
                    : baseUrl + "/api/v1/query?query=" + encoded;

            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null) return null;
            JsonNode root = mapper.readTree(response);
            if (!"success".equalsIgnoreCase(root.path("status").asText())) {
                log.debug("Prometheus query non-success: {}", response);
                return null;
            }
            JsonNode result = root.path("data").path("result");
            if (!result.isArray() || result.isEmpty()) return null;
            JsonNode value = result.get(0).path("value");
            if (value.isArray() && value.size() > 1) {
                return Double.parseDouble(value.get(1).asText());
            }
        } catch (Exception ex) {
            log.error("Prometheus instant query failed for promql={}", promql, ex);
        }
        return null;
    }
}
