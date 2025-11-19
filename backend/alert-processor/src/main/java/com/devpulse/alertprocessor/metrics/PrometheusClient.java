package com.devpulse.alertprocessor.metrics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
public class PrometheusClient {

    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate rest = new RestTemplate();

    public Double queryInstant(String baseUrl, String promql) {
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .path("/api/v1/query")
                    .queryParam("query", promql)
                    .build(true).toUri();
            String body = rest.getForObject(uri, String.class);
            JsonNode root = mapper.readTree(body);
            JsonNode data = root.path("data").path("result");
            if (data.isArray() && !data.isEmpty()) {
                JsonNode value = data.get(0).path("value");
                if (value.isArray() && value.size() > 1) {
                    String sval = value.get(1).asText();
                    return Double.parseDouble(sval);
                }
            }
        } catch (Exception ex) {
            // log but swallow
        }
        return null;
    }
}
