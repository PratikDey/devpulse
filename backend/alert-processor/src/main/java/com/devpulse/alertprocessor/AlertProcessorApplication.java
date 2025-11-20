package com.devpulse.alertprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AlertProcessorApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlertProcessorApplication.class, args);
	}

}
