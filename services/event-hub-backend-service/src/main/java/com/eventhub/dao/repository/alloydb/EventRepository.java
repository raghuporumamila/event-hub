package com.eventhub.dao.repository.alloydb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eventhub.dao.repository.BaseRepository;
import org.springframework.stereotype.Component;

import com.eventhub.model.Event;
import com.eventhub.model.EventCountsByDay;
import com.eventhub.model.EventDefinition;
import com.eventhub.dao.util.DateUtil;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;

@Component(value="eventRepository")
public class EventRepository extends BaseRepository {
	public List<EventDefinition> findDefinitionsByOrgId(String orgId, String workspace) throws Exception {
		List<EventDefinition> definitions = new ArrayList<EventDefinition>();
		ApiFuture<QuerySnapshot> query = db.collection("org_event_definitions").whereEqualTo("orgId", orgId).whereEqualTo("workspace", workspace).get();
		QuerySnapshot querySnapshot = query.get();
		List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
		for (QueryDocumentSnapshot document : documents) {
			EventDefinition definition = new EventDefinition();
			
			//definition.setId(document.getId());
			definition.setOrgId(document.getString("orgId"));
			definition.setSourceId(document.getString("sourceId"));
			definition.setEventName(document.getString("eventName"));
			definition.setWorkspace(document.getString("workspace"));
			definition.setSchema(document.getString("schema"));
			definitions.add(definition);
		}
		return definitions;
	}
	
	public EventDefinition findDefinitionByOrgId(String orgId, String workspace, String eventName) throws Exception {
		EventDefinition definition = new EventDefinition();
		ApiFuture<QuerySnapshot> query = db.collection("org_event_definitions")
				.whereEqualTo("orgId", orgId)
				.whereEqualTo("eventName", eventName)
				.whereEqualTo("workspace", workspace).get();
		QuerySnapshot querySnapshot = query.get();
		List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
		for (QueryDocumentSnapshot document : documents) {
			
			
			//definition.setId(document.getId());
			definition.setOrgId(document.getString("orgId"));
			definition.setSourceId(document.getString("sourceId"));
			definition.setEventName(document.getString("eventName"));
			definition.setWorkspace(document.getString("workspace"));
			definition.setSchema(document.getString("schema"));
			break;
		}
		return definition;
	}
	
	public EventDefinition findDefinition(String id) throws Exception {
		ApiFuture<DocumentSnapshot> future = db.collection("org_event_definitions").document(id).get();
		DocumentSnapshot document = future.get();
		
		EventDefinition definition = new EventDefinition();
		
		//definition.setId(document.getId());
		definition.setOrgId(document.getString("orgId"));
		definition.setSourceId(document.getString("sourceId"));
		definition.setEventName(document.getString("eventName"));
		definition.setWorkspace(document.getString("workspace"));
		definition.setSchema(document.getString("schema"));
		
		return definition;
	}


	public void saveDefinition(EventDefinition definition) throws Exception {
		DocumentReference docRef = null;//db.collection("org_event_definitions").document(definition.getId());
		Map<String, Object> data = new HashMap<>();
        data.put("eventName", definition.getEventName());
        data.put("orgId", definition.getOrgId());
        data.put("schema", definition.getSchema());
        data.put("sourceId", definition.getSourceId());
        data.put("workspace", definition.getWorkspace());
        ApiFuture<WriteResult> result = docRef.set(data);
        result.get().getUpdateTime();
	}
	
	public void updateDefinition(EventDefinition definition) throws Exception {
		DocumentReference docRef = null;//db.collection("org_event_definitions").document(definition.getId());
		Map<String, Object> data = new HashMap<>();
        data.put("eventName", definition.getEventName());
        data.put("orgId", definition.getOrgId());
        data.put("schema", definition.getSchema());
        data.put("sourceId", definition.getSourceId());
        data.put("workspace", definition.getWorkspace());
        ApiFuture<WriteResult> result = docRef.update(data);
        result.get().getUpdateTime();
	}
	
	public void deleteDefinition(String id) throws Exception {
		DocumentReference docRef = db.collection("org_event_definitions").document(id);
		ApiFuture<WriteResult> result = docRef.delete();
        result.get().getUpdateTime();
	}
	
	public List<Event> getLatestEvents(String orgId, String workspace) throws Exception {
		List<Event> latestEvents = new ArrayList<Event>();
		ApiFuture<QuerySnapshot> query = db.collection("org_events").whereEqualTo("orgId", orgId).whereEqualTo("workspace", workspace).get();
		QuerySnapshot querySnapshot = query.get();
		List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
		for (QueryDocumentSnapshot document : documents) {
			Event event = new Event();
			
			event.setName(document.getString("name"));
			com.google.cloud.Timestamp timestamp = document.getTimestamp("timestamp");
			event.setTimestamp(timestamp.toString());
			event.setUserId(document.getString("userId"));
			event.setSourceKey(document.getString("sourceKey"));
			//event.setProperties((Map<String, Object>)document.get("properties"));
			
			latestEvents.add(event);
		}
		
		return latestEvents;
	}
	
	public List<EventCountsByDay> findEventCountsForPast7Days(String orgId, String workspace) throws Exception {
		//Map<String, Long> eventCountsForPast7Days = new HashMap<String, Long>();
		List<EventCountsByDay> eventCountsForPast7Days = getInitializedEventCountsForPast7Days();
		
		ApiFuture<QuerySnapshot> query = db.collection("org_events").whereEqualTo("orgId", orgId)
										.whereEqualTo("workspace", workspace)
										.whereLessThan("timestamp", DateUtil.getCurrentTimestamp())
										.whereGreaterThan("timestamp", DateUtil.get7DaysBackTimestamp())
										.get();
		QuerySnapshot querySnapshot = query.get();
		List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
		for (QueryDocumentSnapshot document : documents) {
			com.google.cloud.Timestamp timestamp = document.getTimestamp("timestamp");
			String dayName = DateUtil.getDayName(timestamp.toDate());
			EventCountsByDay eventCountsByDay = new EventCountsByDay();
			eventCountsByDay.setDayName(dayName);
			if (eventCountsForPast7Days.contains(eventCountsByDay)) {
				EventCountsByDay existingEventCountsByDay= eventCountsForPast7Days.get(eventCountsForPast7Days.indexOf(eventCountsByDay));
				existingEventCountsByDay.setCount(existingEventCountsByDay.getCount() + 1);
			}
		}
		
		return eventCountsForPast7Days;
	}
	
	private List<EventCountsByDay> getInitializedEventCountsForPast7Days() {
		List<EventCountsByDay> eventCountsForPast7Days = new ArrayList<EventCountsByDay>();
		EventCountsByDay eventCountsByDay = new EventCountsByDay();
		eventCountsByDay.setDayName("Monday");
		eventCountsForPast7Days.add(eventCountsByDay);
		eventCountsByDay = new EventCountsByDay();
		eventCountsByDay.setDayName("Tuesday");
		eventCountsForPast7Days.add(eventCountsByDay);
		eventCountsByDay = new EventCountsByDay();
		eventCountsByDay.setDayName("Wednesday");
		eventCountsForPast7Days.add(eventCountsByDay);
		eventCountsByDay = new EventCountsByDay();
		eventCountsByDay.setDayName("Thursday");
		eventCountsForPast7Days.add(eventCountsByDay);
		eventCountsByDay = new EventCountsByDay();
		eventCountsByDay.setDayName("Friday");
		eventCountsForPast7Days.add(eventCountsByDay);
		eventCountsByDay = new EventCountsByDay();
		eventCountsByDay.setDayName("Saturday");
		eventCountsForPast7Days.add(eventCountsByDay);
		eventCountsByDay = new EventCountsByDay();
		eventCountsByDay.setDayName("Sunday");
		eventCountsForPast7Days.add(eventCountsByDay);
		return eventCountsForPast7Days;
	}
}
