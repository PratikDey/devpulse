package com.devpulse.alertprocessor.metrics;

import com.devpulse.common.dto.AlertMessageDto;
import com.devpulse.alertprocessor.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

/**
 * Poll Prometheus on fixed interval, evaluate rules, and create alerts when rules fire.
 * This is rule-driven. Rules are editable in metrics-rules.yml.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@EnableScheduling
public class MetricsPollingScheduler {

    private final MetricRulesLoader loader;
    private final PrometheusClient client;
    private final MetricAlertEvaluator evaluator;
    private final AlertService alertService;

    @Value("${prometheus.base-url:http://localhost:9090}")
    private String prometheusBaseUrl;

    @Value("${metrics.rules-file:classpath:metrics-rules.yml}")
    private String rulesFile;

    private List<MetricRule> rules = Collections.emptyList();
    private final Map<String, Instant> cooldowns = new HashMap<>();

    @Scheduled(fixedDelayString = "${metrics.poll-interval-ms:15000}")
    public void poll() {
        try {
            if (rules.isEmpty()) {
                rules = loader.load(rulesFile);
            }
            for (MetricRule rule : rules) {
                Double value = client.queryInstant(prometheusBaseUrl, rule.getPromql());
                if (value == null) continue;
                boolean fire = evaluator.evaluate(rule, value);
                if (fire && !onCooldown(rule.getId())) {
                    AlertMessageDto dto = AlertMessageDto.builder()
                            .serviceName("prometheus")
                            .severity(rule.getSeverity())
                            .message(rule.getName() + " fired (value=" + value + ")")
                            .timestamp(Instant.now())
                            .ruleId(rule.getId())
                            .details(rule.getDescription())
                            .sourceType("METRIC")
                            .context(Map.of("value", value, "promql", rule.getPromql()))
                            .build();
                    alertService.handleAlert(dto); // direct handling; could be published to Kafka if desired
                    setCooldown(rule.getId(), rule.getDurationSeconds() > 0 ? rule.getDurationSeconds() : 60);
                }
            }
        } catch (Exception ex) {
            log.error("Metrics polling error", ex);
        }
    }

    private boolean onCooldown(String id) {
        Instant until = cooldowns.get(id);
        return until != null && Instant.now().isBefore(until);
    }

    private void setCooldown(String id, int seconds) {
        cooldowns.put(id, Instant.now().plusSeconds(seconds));
    }
}
