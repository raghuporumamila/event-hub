package com.eventhub.dao.repository.alloydb;

import java.util.ArrayList;
import java.util.List;

import com.eventhub.dao.repository.BaseRepository;
import org.springframework.stereotype.Component;

import com.eventhub.model.Consumer;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

@Component(value="consumerRepository")
public class ConsumerRepository extends BaseRepository {
	public List<Consumer> findByOrgId(String orgId, String workspace) throws Exception {
		List<Consumer> consumers = new ArrayList<Consumer>();
		ApiFuture<QuerySnapshot> query = db.collection("consumers").whereEqualTo("orgId", orgId).whereEqualTo("workspace", workspace).get();
		QuerySnapshot querySnapshot = query.get();
		List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
		for (QueryDocumentSnapshot document : documents) {
			Consumer consumer = new Consumer();
			
			consumer.setId(document.getId());
			consumer.setFirstName(document.getString("firstName"));
			consumer.setLastName(document.getString("lastName"));
			consumer.setOrgId(document.getString("orgId"));
			consumer.setUserId(document.getString("userId"));
			consumer.setCity(document.getString("city"));
			consumer.setPostalCode(document.getString("postalCode"));
			consumer.setState(document.getString("state"));
			consumer.setCountry(document.getString("country"));
			consumer.setEmail(document.getString("email"));
			consumers.add(consumer);
		}
		return consumers;
	}
}
