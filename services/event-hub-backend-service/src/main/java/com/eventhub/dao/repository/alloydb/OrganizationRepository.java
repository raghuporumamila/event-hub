package com.eventhub.dao.repository.alloydb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.eventhub.dao.repository.BaseRepository;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Component;

import com.eventhub.model.Organization;
import com.eventhub.model.SourceType;
import com.eventhub.model.Workspace;
import com.eventhub.dao.util.RepositoryUtil;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;

@Component(value="orgRepository")
public interface OrganizationRepository extends ListCrudRepository<Organization, Long> {
    //public List<SourceType> getSourceTypes(String orgId, String workspace);
	/*
	public void save(Organization organization) throws Exception {
		DocumentReference docRef = db.collection("organizations").document(organization.getId());
		Map<String, Object> data = new HashMap<>();
        data.put("name", organization.getName());
        data.put("address", organization.getAddress());
        data.put("address2", organization.getAddress2());
        data.put("city", organization.getCity());
        data.put("country", organization.getCountry());
        data.put("state", organization.getState());
        data.put("postalCode", organization.getPostalCode());
        ApiFuture<WriteResult> result = docRef.set(data);
        result.get().getUpdateTime();
	}
	
	public List<Organization> findAll() throws Exception {
		List<Organization> orgs = new ArrayList<Organization>();
		ApiFuture<QuerySnapshot> query = db.collection("organizations").get();
	    QuerySnapshot querySnapshot = query.get();
	    List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
	    for (QueryDocumentSnapshot document : documents) {
	    	Organization org = getOrg(document);
	    	orgs.add(org);
	    }
		return orgs;
	}
	
	private Organization getOrg(QueryDocumentSnapshot document) {
		Organization org = new Organization();
    	org.setId(document.getId());
    	org.setName(document.getString("name"));
    	org.setAddress(document.getString("address"));
    	org.setAddress2(document.getString("address2"));
    	org.setCity(document.getString("city"));
    	org.setCountry(document.getString("country"));
    	org.setState(document.getString("state"));
    	org.setPostalCode(document.getString("postalCode"));
    	org.setSourceTypes((List<Map<String, String>>) document.get("sourceTypes"));
    	return org;
	}
	
	public Organization findBySourceKey(String sourceKey) throws Exception {
		Organization org = null;
		List<Organization> orgs = findAll();
		for (Organization orgFromDB : orgs) {
			List<Map<String, String>> sourceTypes = orgFromDB.getSourceTypes();
			if (sourceTypes != null) {
				for (Map<String, String> sourceType:sourceTypes) {
					if (sourceType.get("key").equals(sourceKey)) {
						org = orgFromDB;
						break;
					}
				}
			}
		}
		return org;
	}
	public void saveWorkspace(Workspace orgWorkspace) throws Exception {
		DocumentReference docRef = db.collection("organizations").document(orgWorkspace.getOrgId());
		// Atomically add a new region to the "regions" array field.
	    docRef.update("workspaces",
	        FieldValue.arrayUnion(orgWorkspace.getName()));
	}
	
	
	
	public List<Workspace> getWorkspaces(String orgId) throws Exception {
		List<Workspace> workspacesList = new ArrayList<Workspace>();
		DocumentReference docRef = db.collection("organizations").document(orgId);
	    ApiFuture<DocumentSnapshot> future = docRef.get();
	    DocumentSnapshot document = future.get();
	    if (document.exists()) {
	      //System.out.println("Document data: " + document.getData());
	      Object workspacesObj = document.get("workspaces");
	      if (workspacesObj != null) {
	    	  List<String> workspaces = (List<String>) workspacesObj;
	    	  for (String workspaceStr : workspaces) {
	    		  Workspace workspace = new Workspace();
	    		  workspace.setName(workspaceStr);
	    		  workspace.setOrgId(orgId);
	    		  workspacesList.add(workspace);
	    	  }
	      }
	    } 
	    return workspacesList;
	}
	
	public void saveSourceType(String orgId, SourceType sourceType) throws Exception {
		Map<String, String> map = new HashMap<String, String>(); 
		map.put("name", sourceType.getName());
		map.put("type", sourceType.getType());
		map.put("key", RepositoryUtil.getDocumentId());
		map.put("workspace", sourceType.getWorkspace());
		DocumentReference docRef = db.collection("organizations").document(orgId);
	    docRef.update("sourceTypes",
	        FieldValue.arrayUnion(map));
	}
	
	public void deleteSourceType(String orgId, SourceType sourceType) throws Exception {
	    
		DocumentReference docRef = db.collection("organizations").document(orgId);
	    ApiFuture<DocumentSnapshot> future = docRef.get();
	    DocumentSnapshot document = future.get();
	    List<Map<String, String>> sourceTypes = null;
	    boolean sourceTypeFound = false;
	    if (document.exists()) {
	      //System.out.println("Document data: " + document.getData());
	      Object sourceTypesObj = document.get("sourceTypes");
	      if (sourceTypesObj != null) {
	    	  sourceTypes = (List<Map<String, String>>) sourceTypesObj;
	    	  int idx = 0;
	    	  
	    	  for (Map<String, String> sourceTypeMap : sourceTypes) {
		    	  Iterator<String> sourceTypeKeyIt = sourceTypeMap.keySet().iterator();
		    	  while (sourceTypeKeyIt.hasNext()) {
		    		  String key = sourceTypeKeyIt.next();
		    		  String val = sourceTypeMap.get(key);
		    		  if (key.equals("type") && sourceType.getId().equals(val)) {
		    			  sourceTypeFound = true;
		    			  break;
		    		  }
		    	  }
		    	  if  (sourceTypeFound) {
		    		  break;
		    	  }
		    	  idx = idx + 1;
	    	  }
	    	  if (sourceTypeFound) {
	    		  sourceTypes.remove(idx);
	    	  }
	      }
	    } 
	    if (sourceTypeFound) {
	    	docRef.update("sourceTypes", sourceTypes);
	    }
	}
	
	public List<SourceType> getSourceTypes(String orgId, String workspace) throws Exception {
		List<SourceType> sourceTypeList = new ArrayList<SourceType>();
		DocumentReference docRef = db.collection("organizations").document(orgId);
	    ApiFuture<DocumentSnapshot> future = docRef.get();
	    DocumentSnapshot document = future.get();
	    if (document.exists()) {
	      //System.out.println("Document data: " + document.getData());
	      Object sourceTypesObj = document.get("sourceTypes");
	      if (sourceTypesObj != null) {
	    	  List<Map<String, String>> sourceTypes = (List<Map<String, String>>) sourceTypesObj;
	    	  for (Map<String, String> sourceTypeMap : sourceTypes) {
		    	  Iterator<String> sourceTypeKeyIt = sourceTypeMap.keySet().iterator();
		    	  SourceType sourceType = new SourceType();
		    	  while (sourceTypeKeyIt.hasNext()) {
		    		  String key = sourceTypeKeyIt.next();
		    		  String val = sourceTypeMap.get(key);
		    		  if (key.equals("name")) {
		    			  sourceType.setName(val);
		    		  } else if (key.equals("type")) {
		    			  //sourceType.setId(Integer.valueOf(val));
		    		  } else if (key.equals("key")) {
		    			  sourceType.setKey(val);
		    		  } else if (key.equals("workspace")) {
		    			  sourceType.setWorkspace(val);
		    		  } 
		    		  
		    	  }
		    	  if (sourceType.getWorkspace().equals(workspace)) {
		    		  sourceTypeList.add(sourceType);
		    	  }
	    	  }
	      }
	    } 
	    return sourceTypeList;
	}*/
}
