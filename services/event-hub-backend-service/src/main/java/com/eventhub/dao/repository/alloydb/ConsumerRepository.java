package com.eventhub.dao.repository.alloydb;

import java.util.ArrayList;
import java.util.List;

import com.eventhub.dao.repository.BaseRepository;
import com.eventhub.model.EventDefinition;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Component;

import com.eventhub.model.Consumer;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsumerRepository extends ListCrudRepository<Consumer, Long> {
	@Query("SELECT d FROM Consumer d WHERE d.organization.id = :orgId and d.workspace.id=:workspaceId")
	List<Consumer> getConsumers(Long orgId, Long workspaceId);
}
