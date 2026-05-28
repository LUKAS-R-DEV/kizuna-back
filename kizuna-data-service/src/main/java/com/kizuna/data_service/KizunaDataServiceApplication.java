package com.kizuna.data_service;

import com.kizuna.data_service.integration.config.IntegrationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(IntegrationProperties.class)
public class KizunaDataServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(KizunaDataServiceApplication.class, args);
	}

}
