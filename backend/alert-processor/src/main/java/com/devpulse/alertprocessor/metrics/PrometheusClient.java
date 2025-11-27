package com.devpulse.alertprocessor.metrics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * PrometheusClient
 *
 * Executes synchronous "instant" PromQL queries safely using WebClient.
 * Supports all PromQL characters ({}[]()"'= etc.), avoids URI creation errors,
 * and follows Prometheus HTTP API format exactly.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PrometheusClient {

    private final ObjectMapper mapper;
    private final WebClient.Builder webClientBuilder;

    /**
     * Execute a Prometheus instant query and return the first numeric result.
     *
     * @param baseUrl e.g. http://localhost:9090
     * @param promql  raw PromQL expression (NOT encoded)
     * @return Double result or null
     */
    public Double queryInstant(String baseUrl, String promql) {
        WebClient webClient = webClientBuilder.build();
        try {
            if (promql == null || promql.isBlank()) {
                log.error("PromQL expression cannot be empty.");
                return null;
            }

            // Normalize base URL to remove trailing slash
            String base = baseUrl.endsWith("/")
                    ? baseUrl.substring(0, baseUrl.length() - 1)
                    : baseUrl;

            // Perform GET using WebClient's URI template for automatic encoding
            String response = webClient.get()
                    .uri(base + "/api/v1/query?query={promql}", promql)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null) {
                log.error("Prometheus returned null response for query: {}", promql);
                return null;
            }

            JsonNode root = mapper.readTree(response);

            if (!"success".equalsIgnoreCase(root.path("status").asText())) {
                log.error("Prometheus returned non-success response: {}", response);
                return null;
            }

            JsonNode resultArray = root.path("data").path("result");
            if (!resultArray.isArray() || resultArray.isEmpty()) {
                log.debug("Prometheus returned empty result for promql={}", promql);
                return null;
            }

            JsonNode valueArray = resultArray.get(0).path("value");
            if (valueArray.isArray() && valueArray.size() >= 2) {
                String numeric = valueArray.get(1).asText();
                try {
                    return Double.parseDouble(numeric);
                } catch (NumberFormatException ex) {
                    log.error("Failed to parse Prometheus value '{}'", numeric);
                }
            }

            log.error("Unexpected Prometheus result format: {}", resultArray);
            return null;

        } catch (Exception ex) {
            log.error("Prometheus instant query failed. promql={}", promql, ex);
            return null;
        }
    }
}
