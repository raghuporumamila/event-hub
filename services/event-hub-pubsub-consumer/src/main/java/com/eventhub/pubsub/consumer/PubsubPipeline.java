package com.eventhub.pubsub.consumer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.datastore.DatastoreIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;

import com.google.datastore.v1.Entity;
import com.google.datastore.v1.Key;
import com.google.datastore.v1.Value;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import static com.google.datastore.v1.client.DatastoreHelper.makeKey;

public class PubsubPipeline {
	private static final String PROJECT_ID = "event-hub-249001";
	
	public static String getDocumentId() {
		UUID uuid = UUID.randomUUID();
        String randomUUIDString = uuid.toString();
        return randomUUIDString;
	}
	
	public static void main(String[] args) throws Exception {
		PubsubOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(PubsubOptions.class);
		Pipeline pipeline = Pipeline.create(options);
		
		//Get the JSON lines from pub sub topic
		PCollection<String> lines = pipeline
				.apply(PubsubIO.readStrings()
						.fromSubscription(options.getSubscriptionName()));
		
		PCollection<Entity> entities = lines.apply(ParDo.of(new DoFn<String, Entity>() {
			@ProcessElement
		    public void processElement(ProcessContext c) {
				String json = c.element();
				JsonElement jsonData = new JsonParser().parse(json);
				JsonObject jsonObject = jsonData.getAsJsonObject();
				Key.Builder keyBuilder = makeKey("org_events", UUID.randomUUID().toString());
				Entity.Builder entityBuilder = Entity.newBuilder();
				entityBuilder.setKey(keyBuilder.build());
				
				Map<String, Value> propertyMap = new HashMap<String, Value>();
				Iterator<Entry<String, JsonElement>> it = jsonObject.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, JsonElement> entry = it.next();
					propertyMap.put(entry.getKey(), Value.newBuilder().setStringValue(entry.getValue().getAsString()).build());
				}
				entityBuilder.putAllProperties(propertyMap);
				Entity entity = entityBuilder.build();
				c.output(entity);
			}
		}));
		entities.apply(DatastoreIO.v1().write().withProjectId(PROJECT_ID));
		pipeline.run();
	}
	
	
}
