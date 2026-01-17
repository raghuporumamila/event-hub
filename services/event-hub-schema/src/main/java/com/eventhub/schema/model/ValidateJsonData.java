package com.eventhub.schema.model;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import com.fasterxml.jackson.databind.JsonNode;

@Data
public class ValidateJsonData {
    private String schema;
    private String payload;

    @JsonProperty("schema")
    public void setSchema(Object schema) {
        if (schema instanceof String) {
            this.schema = (String) schema;
        } else {
            // Converts the Map/Object back into a raw JSON String
            this.schema = new org.json.JSONObject((java.util.Map<?, ?>) schema).toString();
        }
    }

    @JsonProperty("payload")
    public void setPayload(Object payload) {
        if (payload instanceof String) {
            this.payload = (String) payload;
        } else {
            this.payload = new org.json.JSONObject((java.util.Map<?, ?>) payload).toString();
        }
    }
}
