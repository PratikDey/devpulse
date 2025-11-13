package com.devpulse.logcollector.controller;

import com.devpulse.common.dto.ApiResponse;
import com.devpulse.logcollector.model.LogDocument;
import com.devpulse.logcollector.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogRepository logRepository;

    @GetMapping
    public ApiResponse<List<LogDocument>> getAll() {
        return ApiResponse.<List<LogDocument>>builder()
                .success(true)
                .message("All logs")
                .data(logRepository.findAll())
                .build();
    }
}