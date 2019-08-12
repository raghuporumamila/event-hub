package com.eventhub.schema.validator;

import java.io.File;
import java.nio.file.Files;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component("jsonSchemaValidator")
public class JSONSchemaValidator {
	
	public void validate(JSONObject schema, JSONObject json) {
		Schema schemaObj = SchemaLoader.load(schema);
		schemaObj.validate(json);
	}

	public void validate(String schema, String json) {
		validate(new JSONObject(schema), new JSONObject(json));
	}
	
	public void validate(File schema, File json) throws Exception {
		validate(readFileAsString(schema), readFileAsString(json));
	}
	
	private String readFileAsString(File file) throws Exception {
		Stream<String> lines = null;
		String content = null;
		try {
			lines = Files.lines(file.toPath());
			content = lines.collect(Collectors.joining(System.lineSeparator()));
		} finally {
			if (lines != null) {
				lines.close();
			}
		}
		return content;
	}
}
