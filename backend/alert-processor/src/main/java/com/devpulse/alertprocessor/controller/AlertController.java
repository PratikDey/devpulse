package com.devpulse.alertprocessor.controller;

import com.devpulse.alertprocessor.model.AlertDocument;
import com.devpulse.alertprocessor.repository.AlertRepository;
import com.devpulse.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertRepository alertRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AlertDocument>>> getAllAlerts() {
        List<AlertDocument> alerts = alertRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
        return ResponseEntity.ok(ApiResponse.<List<AlertDocument>>builder()
                .success(true)
                .message("Alerts fetched successfully")
                .data(alerts)
                .build());
    }
}
