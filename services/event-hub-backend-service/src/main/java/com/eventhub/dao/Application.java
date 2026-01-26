package com.eventhub.dao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

import java.util.Collections;

@SpringBootApplication
@EntityScan(basePackages = "com.eventhub.model")
public class Application {
	public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.setDefaultProperties(Collections.singletonMap("server.port", "8081"));
        app.run(args);
        //SpringApplication.run(Application.class, args);
    }
}
