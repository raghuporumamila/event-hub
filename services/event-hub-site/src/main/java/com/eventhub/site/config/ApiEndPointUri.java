package com.eventhub.site.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@ConfigurationProperties("evenhub.rest.client")
@Getter
@Setter
public class ApiEndPointUri {

	private String daoApiEndpoint;
	private String schemaApiEndpoint;
	private String publisherApiEndpoint;
	private String genAIAPIEndpoint;

    @Profile("local")
	@Bean
	public String localRestClientProperties() {
		
		System.out.println("daoApiEndpoint == " + daoApiEndpoint);
		System.out.println("schemaApiEndpoint == " + schemaApiEndpoint);
		System.out.println("publisherApiEndpoint == " + publisherApiEndpoint);
		
		return "Rest Client properties - Local"; 
	}
	
	@Profile("gcp")
	@Bean
	public String gcpRestClientProperties() {
		
		System.out.println("daoApiEndpoint == " + daoApiEndpoint);
		System.out.println("schemaApiEndpoint == " + schemaApiEndpoint);
		System.out.println("publisherApiEndpoint == " + publisherApiEndpoint);
		
		return "Rest Client properties - GCP"; 
	}
}
