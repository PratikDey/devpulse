package com.devpulse.alertprocessor.service;

import com.devpulse.common.dto.AlertMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Simple email sender. Externalize recipients in production.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailAlertService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmailId;

    public void sendAlertEmail(AlertMessageDto dto) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmailId);
            msg.setTo("devteam@example.com"); // TODO: make configurable
            msg.setSubject("DevPulse Alert: " + dto.getSeverity() + " - " + dto.getServiceName());
            msg.setText(dto.getMessage() + "\n\nDetails: " + dto.getDetails());
            mailSender.send(msg);
            log.info("Email sent for alert {}:{}", dto.getServiceName(), dto.getRuleId());
        } catch (Exception ex) {
            log.error("Error sending alert email", ex);
        }
    }
}
