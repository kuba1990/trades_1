package com.verygoodbank.tes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TradeEnrichmentServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(TradeEnrichmentServiceApplication.class, args);
	}
}
