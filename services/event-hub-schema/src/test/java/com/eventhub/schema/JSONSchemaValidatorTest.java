package com.eventhub.schema;

import java.io.File;

import com.eventhub.schema.validator.JSONSchemaValidator;

import junit.framework.TestCase;

public class JSONSchemaValidatorTest extends TestCase {

	public void testValidate() {
		
	}
	
	public void testValidateForFiles() throws Exception {
		JSONSchemaValidator jsonSchemaValidator = new JSONSchemaValidator();
		File eventSchema = new File("/Users/raghu/Desktop/EventHub/SampleEventSchema.json");
		File eventData = new File("/Users/raghu/Desktop/EventHub/SampleEventData.json");
		jsonSchemaValidator.validate(eventSchema, eventData);	
	}
}
