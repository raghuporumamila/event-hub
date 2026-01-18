package com.eventhub.dao.repository.alloydb;

import com.eventhub.model.Source;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SourceRepository extends ListCrudRepository<Source, Long> {
    @Query("SELECT d FROM Source d WHERE d.organization.id = :orgId and d.workspace.id=:workspaceId")
    List<Source> getSources(Long orgId, Long workspaceId);
}
