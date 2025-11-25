package com.devpulse.alertprocessor.service;

import com.devpulse.common.constants.EmailConstants;
import com.devpulse.common.dto.AlertMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * Compose and send a simple alert email using
     * {@link org.springframework.mail.javamail.JavaMailSender}.
     *
     * Important notes:
     * - This method is synchronous and will block until the mail sender completes.
     * Consider annotating with @Async
     * or delegating to a task executor for non-blocking behavior in production.
     * - Recipient address is currently hard-coded; move to configurable property or
     * address book.
     *
     * @param dto alert DTO used to populate subject and body
     */

    public void sendAlertEmail(AlertMessageDto dto) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(EmailConstants.FROM_EMAIL_ID);
            msg.setTo(EmailConstants.TO_EMAIL_ID);
            msg.setSubject("DevPulse Alert: " + dto.getSeverity() + " - " + dto.getServiceName());
            msg.setText(dto.getMessage() + "\n\nDetails: " + dto.getDetails());
            mailSender.send(msg);
            log.info("Email sent for alert {}:{}", dto.getServiceName(), dto.getRuleId());
        } catch (Exception ex) {
            log.error("Error sending alert email", ex);
        }
    }
}
