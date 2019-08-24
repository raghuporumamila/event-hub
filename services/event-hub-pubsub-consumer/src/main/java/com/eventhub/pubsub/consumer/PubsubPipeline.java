package com.eventhub.pubsub.consumer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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

import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import com.google.datastore.v1.Entity;
import com.google.datastore.v1.Key;
import com.google.datastore.v1.Value;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.Descriptors.FieldDescriptor;

import static com.google.datastore.v1.client.DatastoreHelper.makeKey;
import static com.google.datastore.v1.client.DatastoreHelper.makeValue;

import static com.google.protobuf.util.Timestamps.fromMillis;
import static java.lang.System.currentTimeMillis;
import com.google.protobuf.Timestamp;

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
		PCollection<PubsubMessage> messages = pipeline
				.apply(PubsubIO.readMessagesWithAttributes()
						.fromSubscription(options.getSubscriptionName()));
		
		/*PCollection<String> lines = pipeline
				.apply(PubsubIO.readStrings()
						.fromSubscription(options.getSubscriptionName()));*/
		
		PCollection<Entity> entities = messages.apply(ParDo.of(new DoFn<PubsubMessage, Entity>() {
			@ProcessElement
		    public void processElement(ProcessContext c) throws Exception {
				String json = new String(c.element().getPayload());
				JsonElement jsonData = new JsonParser().parse(json);
				System.out.println(json);
				JsonObject jsonObject = jsonData.getAsJsonObject();
				Key.Builder keyBuilder = makeKey("org_events", UUID.randomUUID().toString());
				Entity.Builder entityBuilder = Entity.newBuilder();
				entityBuilder.setKey(keyBuilder.build());
				//Map<String, String> attributesMap = c.element().getAttribute();
				Map<String, Value> propertyMap = new HashMap<String, Value>();
				System.out.println("Org ID == " + c.element().getAttribute("orgId"));
				System.out.println("Workspace == " + c.element().getAttribute("workspace"));
				propertyMap.put("orgId", Value.newBuilder().setStringValue(c.element().getAttribute("orgId")).build());
				propertyMap.put("workspace", Value.newBuilder().setStringValue(c.element().getAttribute("workspace")).build());
				
				Iterator<Entry<String, JsonElement>> it = jsonObject.entrySet().iterator();
				
				while (it.hasNext()) {
					Entry<String, JsonElement> entry = it.next();
					if (!entry.getKey().equals("properties")) {
						propertyMap.put(entry.getKey(), Value.newBuilder().setStringValue(entry.getValue().getAsString()).build());
						if (entry.getKey().startsWith("timestamp")) {
							DateFormat m_ISO8601Local = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
							Date date = m_ISO8601Local.parse(entry.getValue().getAsString());
							com.google.protobuf.Timestamp timestamp = fromMillis(date.getTime());
							propertyMap.put(entry.getKey(), Value.newBuilder().setTimestampValue(timestamp).build());
						}
					} else {
						
						Value val = Value.newBuilder().setEntityValue(getEmbeddedEntity(entry)).build();
						propertyMap.put(entry.getKey(), val);
					}
				}
				
				entityBuilder.putAllProperties(propertyMap);
				Entity entity = entityBuilder.build();
				c.output(entity);
			}
		}));
		entities.apply(DatastoreIO.v1().write().withProjectId(PROJECT_ID));
		pipeline.run();
	}
	
	private static Entity getEmbeddedEntity(Entry<String, JsonElement> entry) {
		JsonObject firstLevelJsonObj = entry.getValue().getAsJsonObject();
		Iterator<Entry<String, JsonElement>> firstLevelIt = firstLevelJsonObj.entrySet().iterator();
		Map<String, Value> firstLevelPropertyMap = new HashMap<String, Value>();
		while (firstLevelIt.hasNext()) {
			Entry<String, JsonElement> firstLevelEntry = firstLevelIt.next();
			firstLevelPropertyMap.put(firstLevelEntry.getKey(), Value.newBuilder().setStringValue(firstLevelEntry.getValue().getAsString()).build());
		}
		
		Entity.Builder entityBuilder = Entity.newBuilder();
		entityBuilder.putAllProperties(firstLevelPropertyMap);
		Entity entity = entityBuilder.build();
		return entity;
	}
	
}
