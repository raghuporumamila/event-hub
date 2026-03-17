package com.eventhub.schema;

import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Application {
	public static void main(String[] args) {
		//SpringApplication.run(Application.class, args);
		SpringApplication app = new SpringApplication(Application.class);

        app.setDefaultProperties(Collections
                .singletonMap("server.port", "8082"));
              app.run(args);
    }
}
