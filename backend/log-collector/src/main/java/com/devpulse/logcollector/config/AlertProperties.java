package com.devpulse.logcollector.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Configuration holder for alert rules and thresholds.
 * Values are loaded from application.yml under prefix "app.alert".
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.alert")
public class AlertProperties {

    /**
     * Immediate keyword-based alerts when an ERROR contains any of these keywords.
     */
    private List<String> keywords;

    /**
     * Spike detection thresholds
     */
    private Spike spike = new Spike();

    /**
     * Consecutive identical message detection
     */
    private Consecutive consecutive = new Consecutive();

    /**
     * Silence detection
     */
    private Silence silence = new Silence();

    @Data
    public static class Spike {
        private int count = 10;
        private int windowSeconds = 30;
        private int cooldownSeconds = 60;
    }

    @Data
    public static class Consecutive {
        private int count = 5;
        private int cooldownSeconds = 60;
    }

    @Data
    public static class Silence {
        private int thresholdSeconds = 120;
    }
}
