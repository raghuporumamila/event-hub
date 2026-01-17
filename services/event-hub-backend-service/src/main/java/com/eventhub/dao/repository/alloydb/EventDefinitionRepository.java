package com.eventhub.dao.repository.alloydb;

import com.eventhub.model.EventDefinition;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventDefinitionRepository extends ListCrudRepository<EventDefinition, Long> {
    @Query("SELECT d FROM EventDefinition d WHERE d.organization.id = :orgId and d.workspace.id=:workspaceId")
    List<EventDefinition> getEventDefinitions(Long orgId, Long workspaceId);
}
