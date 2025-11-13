package com.emysore.ecom_mysore_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EcomMysoreBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcomMysoreBackendApplication.class, args);
	}

}
