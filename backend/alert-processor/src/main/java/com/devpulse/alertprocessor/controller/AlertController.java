package com.devpulse.alertprocessor.controller;

import com.devpulse.alertprocessor.model.AlertDocument;
import com.devpulse.alertprocessor.repository.AlertRepository;
import com.devpulse.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Simple REST controller to query persisted alerts for the dashboard.
 */
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertRepository repository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AlertDocument>>> getAll() {
        List<AlertDocument> list = repository.findAll();
        ApiResponse<List<AlertDocument>> resp = ApiResponse.<List<AlertDocument>>builder()
                .success(true)
                .message("Alerts fetched")
                .data(list)
                .build();
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/service/{serviceName}")
    public ResponseEntity<ApiResponse<List<AlertDocument>>> getByService(@PathVariable("serviceName") String serviceName) {
        List<AlertDocument> list = repository.findByServiceName(serviceName);
        ApiResponse<List<AlertDocument>> resp = ApiResponse.<List<AlertDocument>>builder()
                .success(true)
                .message("Alerts for " + serviceName)
                .data(list)
                .build();
        return ResponseEntity.ok(resp);
    }
}
