package com.eventhub.schema.controller;

import com.eventhub.schema.model.ValidateJsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.eventhub.schema.validator.JSONSchemaValidator;
import org.springframework.http.HttpStatus;

@Controller
public class ValidateController {
	
	@Autowired
	private JSONSchemaValidator jsonSchemaValidator;
	
	@PostMapping(value="/validate")
	@ResponseStatus(HttpStatus.OK)
    public void validate(@RequestBody ValidateJsonData validateJsonData) {
		jsonSchemaValidator.validate(validateJsonData.getSchema(), validateJsonData.getPayload());
	}	
	
	@PostMapping(value="/validateData")
	@ResponseStatus(HttpStatus.OK)
    public void validateData(@RequestBody String jsonData) {
		System.out.println("JSON data == " + jsonData);
		//String jsonSchema = null;
		//jsonSchemaValidator.validate(jsonSchema, jsonData);
	}	
}
