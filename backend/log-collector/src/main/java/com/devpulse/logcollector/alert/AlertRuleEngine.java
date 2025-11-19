package com.devpulse.logcollector.alert;

import com.devpulse.common.dto.AlertMessageDto;
import com.devpulse.common.dto.LogMessageDto;
import com.devpulse.common.enums.AlertSeverity;
import com.devpulse.common.enums.LogLevel;
import com.devpulse.logcollector.config.AlertProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AlertRuleEngine
 *
 * Hybrid rule engine (sliding window + consecutive count + keywords + silence)
 * - onLog(...) must be called for every persisted LogMessageDto
 * - publishes AlertMessageDto via AlertPublisher
 *
 * Lightweight in-memory implementation suitable for MVP/hackathon.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlertRuleEngine {

    private final AlertProperties props;
    private final AlertPublisher publisher;

    // Sliding window per service: deque of Instants when ERROR logged
    private final Map<String, Deque<Instant>> errorWindows = new ConcurrentHashMap<>();

    // Last seen time per service (for silence detection)
    private final Map<String, Instant> lastSeen = new ConcurrentHashMap<>();

    // Cooldown timestamps per service+rule (to avoid floods)
    private final Map<String, Instant> cooldowns = new ConcurrentHashMap<>();

    // Consecutive message tracking: service -> (lastMessage, count)
    private final Map<String, String> lastMessage = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> consecutiveCount = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("AlertRuleEngine initialized with props: {}", props);
    }

    /**
     * Called for every parsed and persisted log.
     * Keep this fast — it updates in-memory state and publishes alerts when rules match.
     */
    public void onLog(LogMessageDto dto) {
        if (dto == null || dto.getLevel() == null) return;

        String svc = dto.getServiceName();
        lastSeen.put(svc, Instant.now());

        // ignore INFO-level logs explicitly
        if (dto.getLevel() == LogLevel.INFO) {
            return;
        }

        // Immediate CRITICAL -> push alert
        if ("CRITICAL".equalsIgnoreCase(dto.getLevel().name())) {
            publishImmediateAlert(dto, "CRITICAL_LOG");
            return;
        }

        // If ERROR: check keyword immediate, sliding window, and consecutive
        if (dto.getLevel() == LogLevel.ERROR) {

            // 1) keyword immediate
            if (isKeywordMatch(dto.getMessage())) {
                publishImmediateAlert(dto, "ERROR_KEYWORD");
                return;
            }

            // 2) sliding window spike detection
            handleSlidingWindow(svc, dto);

            // 3) consecutive identical message detection
            handleConsecutive(svc, dto);
        }
    }

    private boolean isKeywordMatch(String message) {
        if (message == null || props.getKeywords() == null) return false;
        String lower = message.toLowerCase();
        for (String kw : props.getKeywords()) {
            if (kw != null && !kw.isEmpty() && lower.contains(kw.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private void publishImmediateAlert(LogMessageDto dto, String ruleId) {
        if (isOnCooldown(dto.getServiceName(), ruleId)) {
            log.debug("Alert suppressed by cooldown ({}:{})", dto.getServiceName(), ruleId);
            return;
        }
        AlertMessageDto alert = AlertMessageDto.builder()
                .serviceName(dto.getServiceName())
                .severity(AlertSeverity.CRITICAL)
                .message(dto.getMessage())
                .timestamp(Instant.now())
                .traceId(dto.getTraceId())
                .ruleId(ruleId)
                .details("Immediate rule triggered: " + ruleId)
                .sourceType("LOG")
                .context(Map.of(
                        "originalTimestamp", dto.getTimestamp(),
                        "level", dto.getLevel()
                ))
                .build();
        publisher.publish(alert);
        setCooldown(dto.getServiceName(), ruleId, props.getSpike().getCooldownSeconds());
    }

    private void handleSlidingWindow(String svc, LogMessageDto dto) {
        Deque<Instant> deque = errorWindows.computeIfAbsent(svc, k -> new ConcurrentLinkedDeque<>());
        Instant now = Instant.now();
        deque.addLast(now);

        // prune older than windowSeconds
        int windowSec = props.getSpike().getWindowSeconds();
        Instant cutoff = now.minus(windowSec, ChronoUnit.SECONDS);
        while (!deque.isEmpty() && deque.peekFirst().isBefore(cutoff)) {
            deque.pollFirst();
        }

        int count = deque.size();
        if (count >= props.getSpike().getCount()) {
            String ruleId = "ERR_SPIKE_" + props.getSpike().getCount() + "_" + props.getSpike().getWindowSeconds();
            if (!isOnCooldown(svc, ruleId)) {
                AlertMessageDto alert = AlertMessageDto.builder()
                        .serviceName(svc)
                        .severity(AlertSeverity.WARNING)
                        .message(String.format("%d ERROR logs in %d seconds", count, props.getSpike().getWindowSeconds()))
                        .timestamp(now)
                        .ruleId(ruleId)
                        .details("Sliding-window spike detection")
                        .sourceType("LOG")
                        .context(Map.of(
                                "errorCount", count,
                                "windowSeconds", props.getSpike().getWindowSeconds()
                        ))
                        .build();
                publisher.publish(alert);
                setCooldown(svc, ruleId, props.getSpike().getCooldownSeconds());
                // clear window to avoid duplicate immediate spikes until cooldown expires
                deque.clear();
            } else {
                log.debug("Spike condition detected but in cooldown for service {}", svc);
            }
        }
    }

    private void handleConsecutive(String svc, LogMessageDto dto) {
        String msg = dto.getMessage() == null ? "<null>" : dto.getMessage();
        String prev = lastMessage.get(svc);
        if (msg.equals(prev)) {
            AtomicInteger cnt = consecutiveCount.computeIfAbsent(svc, k -> new AtomicInteger(0));
            int nowCnt = cnt.incrementAndGet();
            if (nowCnt >= props.getConsecutive().getCount()) {
                String ruleId = "CONSECUTIVE_" + props.getConsecutive().getCount();
                if (!isOnCooldown(svc, ruleId)) {
                    AlertMessageDto alert = AlertMessageDto.builder()
                            .serviceName(svc)
                            .severity(AlertSeverity.WARNING)
                            .message("Repeated identical error: " + msg)
                            .timestamp(Instant.now())
                            .ruleId(ruleId)
                            .details("Consecutive identical message rule")
                            .sourceType("LOG")
                            .context(Map.of(
                                    "consecutiveCount", nowCnt
                            ))
                            .build();
                    publisher.publish(alert);
                    setCooldown(svc, ruleId, props.getConsecutive().getCooldownSeconds());
                    cnt.set(0);
                }
            }
        } else {
            // reset
            lastMessage.put(svc, msg);
            consecutiveCount.put(svc, new AtomicInteger(1));
        }
    }

    private boolean isOnCooldown(String svc, String ruleId) {
        String key = svc + "::" + ruleId;
        Instant until = cooldowns.get(key);
        return until != null && Instant.now().isBefore(until);
    }

    private void setCooldown(String svc, String ruleId, int seconds) {
        String key = svc + "::" + ruleId;
        cooldowns.put(key, Instant.now().plus(seconds, ChronoUnit.SECONDS));
    }

    /**
     * Scheduled silence detection:
     * - runs periodically and checks lastSeen map for services that haven't reported logs
     * - when silence threshold exceeded, publish SERVICE_DOWN alert
     */
    @Scheduled(fixedDelayString = "${app.alert.silence.threshold-check-ms:30000}")
    public void checkSilence() {
        int threshold = props.getSilence().getThresholdSeconds();
        Instant now = Instant.now();
        for (Map.Entry<String, Instant> e : lastSeen.entrySet()) {
            String svc = e.getKey();
            Instant seen = e.getValue();
            if (seen == null) continue;
            long secs = ChronoUnit.SECONDS.between(seen, now);
            if (secs >= threshold) {
                String ruleId = "SERVICE_SILENCE_" + threshold;
                if (!isOnCooldown(svc, ruleId)) {
                    AlertMessageDto alert = AlertMessageDto.builder()
                            .serviceName(svc)
                            .severity(AlertSeverity.CRITICAL)
                            .message("No logs received for " + secs + " seconds (threshold=" + threshold + "s)")
                            .timestamp(now)
                            .ruleId(ruleId)
                            .details("Service silence detection")
                            .sourceType("LOG")
                            .context(Map.of("lastSeenSecondsAgo", secs))
                            .build();
                    publisher.publish(alert);
                    setCooldown(svc, ruleId, props.getSpike().getCooldownSeconds());
                }
            }
        }
    }
}