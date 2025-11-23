package com.devpulse.logdashboard.controller;

import com.devpulse.common.dto.ApiResponse;
import com.devpulse.common.dto.LogResponseDto;
import com.devpulse.logdashboard.service.LogQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * LogController
 *
 * - GET /api/logs?page=&size=         => paged logs
 * - GET /api/logs/service/{service}   => logs by service
 * - GET /api/logs/level/{level}      => logs by level (INFO/WARN/ERROR/DEBUG)
 * - GET /api/logs/recent             => top 100 recent logs
 * - GET /api/logs/stream             => Server-Sent Events (SSE) real-time stream of recent logs
 *
 * SSE approach keeps client simple (no STOMP).
 */
@Slf4j
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogQueryService queryService;

    // Keep a simple broadcaster for SSE clients
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        var result = queryService.findAll(page, size);
        var resp = ApiResponse.<Object>builder().success(true).message("Logs fetched").data(result).build();
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/service/{serviceName}")
    public ResponseEntity<ApiResponse<?>> getByService(
            @PathVariable("serviceName") String serviceName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        var result = queryService.findByService(serviceName, page, size);
        var resp = ApiResponse.<Object>builder().success(true).message("Logs for service").data(result).build();
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/level/{level}")
    public ResponseEntity<ApiResponse<?>> getByLevel(
            @PathVariable("level") String level,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        var result = queryService.findByLevel(level, page, size);
        var resp = ApiResponse.<Object>builder().success(true).message("Logs by level").data(result).build();
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<?>> recent() {
        List<LogResponseDto> list = queryService.recentTop100();
        var resp = ApiResponse.<Object>builder().success(true).message("Recent logs").data(list).build();
        return ResponseEntity.ok(resp);
    }

    /**
     * SSE streaming endpoint — clients connect and receive pushed events.
     * Implementation: log-collector (or alert-processor) can POST to an internal endpoint to broadcast new logs,
     * or we can wire direct DB tailable cursor later. For now log-collector can POST to /api/logs/push (below).
     */
    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(0L); // no timeout
        emitters.add(emitter);

        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onCompletion(() -> emitters.remove(emitter));

        return emitter;
    }

    /**
     * Internal push endpoint: log-collector (or producer) can call this to push a newly persisted log to all SSE clients.
     * Protect this endpoint later (internal network or auth).
     */
    @PostMapping("/push")
    public ResponseEntity<ApiResponse<?>> push(@RequestBody LogResponseDto dto) {
        // Broadcast to all active emitters
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("log").data(dto));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
        return ResponseEntity.ok(ApiResponse.<Object>builder().success(true).message("Pushed").data(null).build());
    }

    /**
     * Optional: range query by timestamp (ISO-8601 strings)
     */
    @GetMapping("/range")
    public ResponseEntity<ApiResponse<?>> range(
            @RequestParam("from") String fromIso,
            @RequestParam("to") String toIso,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Instant from = Instant.parse(fromIso);
        Instant to = Instant.parse(toIso);
        var result = queryService.findBetween(new LogQueryService.InstantRange(from, to), page, size);
        var resp = ApiResponse.<Object>builder().success(true).message("Logs in range").data(result).build();
        return ResponseEntity.ok(resp);
    }
}
