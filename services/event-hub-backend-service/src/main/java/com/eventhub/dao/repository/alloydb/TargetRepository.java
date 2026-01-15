package com.eventhub.dao.repository.alloydb;

import java.util.ArrayList;
import java.util.List;

import com.eventhub.dao.repository.BaseRepository;
import org.springframework.stereotype.Component;

import com.eventhub.model.Target;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
@Component(value="targetRepository")
public class TargetRepository extends BaseRepository {

	public List<Target> findAll() throws Exception {
		List<Target> targets = new ArrayList<Target>();
		ApiFuture<QuerySnapshot> query = db.collection("targets").get();
	    QuerySnapshot querySnapshot = query.get();
	    List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
	    for (QueryDocumentSnapshot document : documents) {
	    	Target target = new Target();
	    	target.setId(document.getId());
	    	target.setName(document.getString("name"));
	    	target.setConfig(document.getString("configTemplate"));
	    	targets.add(target);
	    }
		return targets;
	}
	
	public List<Target> findByOrgId(String orgId) throws Exception {
		List<Target> targets = new ArrayList<Target>();
		ApiFuture<QuerySnapshot> query = db.collection("org_target_configs").whereEqualTo("orgId", orgId).get();
		QuerySnapshot querySnapshot = query.get();
		List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
		for (QueryDocumentSnapshot document : documents) {
			Target target = new Target();
			target.setId(document.getId());
	    	target.setName(document.getString("name"));
	    	target.setConfig(document.getString("config"));
	    	target.setParentId(document.getString("targetId"));
			targets.add(target);
		}
		return targets;
	}
}
