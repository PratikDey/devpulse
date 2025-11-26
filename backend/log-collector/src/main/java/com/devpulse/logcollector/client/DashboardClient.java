package com.devpulse.logcollector.client;

import com.devpulse.common.dto.LogMessageDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "log-dashboard", url = "${app.dashboard-url}")
public interface DashboardClient {

    @PostMapping("/api/logs/push")
    void pushLog(@RequestBody LogMessageDto logDto);
}
