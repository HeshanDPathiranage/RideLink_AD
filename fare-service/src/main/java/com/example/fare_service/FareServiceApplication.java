package com.example.fare_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@EnableMongoAuditing
@SpringBootApplication
public class FareServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FareServiceApplication.class, args);
	}

}
