package com.eventhub.dao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = "com.eventhub.model")
public class Application {
	public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
