package com.devpulse.logcollector.controller;

import com.devpulse.common.dto.ApiResponse;
import com.devpulse.logcollector.model.InvalidLogDocument;
import com.devpulse.logcollector.repository.InvalidLogRepository;
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
@RequestMapping("/api/logs/invalid")
@RequiredArgsConstructor
public class InvalidLogController {

    private final InvalidLogRepository repository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InvalidLogDocument>>> getInvalidLogs() {
        List<InvalidLogDocument> logs = repository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
        return ResponseEntity.ok(ApiResponse.<List<InvalidLogDocument>>builder()
                .success(true)
                .message("Invalid logs fetched successfully")
                .data(logs)
                .build());
    }
}
