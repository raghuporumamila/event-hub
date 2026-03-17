package com.eventhub.dao.repository.alloydb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eventhub.dao.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Component;

import com.eventhub.model.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;

@Component(value="userRepository")
public interface UserRepository extends ListCrudRepository<User, Long> {
	@Query("SELECT d FROM User d WHERE d.email = :email")
	User findByEmail(String email);
	@Query("SELECT d FROM User d WHERE d.organization = :orgId")
	List<User> findAllByOrgId(String orgId);
	/*
	public void save(User user) throws Exception {
		DocumentReference docRef = db.collection("users").document(user.getId());
		Map<String, Object> data = new HashMap<>();
        data.put("email", user.getEmail());
        data.put("orgId", user.getOrgId());
        data.put("role", user.getRole());
        data.put("defaultWorkSpace", user.getDefaultWorkspace());
        
        ApiFuture<WriteResult> result = docRef.set(data);
        result.get().getUpdateTime();
	}
	
	public List<User> findByOrgId(String orgId) throws Exception {
		List<User> users = new ArrayList<User>();
		ApiFuture<QuerySnapshot> query = db.collection("users").whereEqualTo("orgId", orgId).get();
		QuerySnapshot querySnapshot = query.get();
		List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
		for (QueryDocumentSnapshot document : documents) {
			User user = new User();
			user.setId(document.getId());
			user.setEmail(document.getString("email"));
			user.setOrgId(orgId);

			users.add(user);
		}
		return users;
	}
	
	public User findByEmail(String email) throws Exception {
		User user = null;
		ApiFuture<QuerySnapshot> query = db.collection("users").whereEqualTo("email", email).get();
		QuerySnapshot querySnapshot = query.get();
		List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
		for (QueryDocumentSnapshot document : documents) {
			user = new User();
			user.setId(document.getId());
			user.setEmail(document.getString("email"));
			user.setOrgId(document.getString("orgId"));
			user.setDefaultWorkspace(document.getString("defaultWorkSpace"));
			user.setRole(document.getString("role"));
			break;
		}
		return user;
	}
	
	public void changeWorkspace(String userId, String workspace) {
		DocumentReference docRef = db.collection("users").document(userId);
		// Atomically add a new region to the "regions" array field.
	    docRef.update("defaultWorkSpace", workspace);
	}*/


}
