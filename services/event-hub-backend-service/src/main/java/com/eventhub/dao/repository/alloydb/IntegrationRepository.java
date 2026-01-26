package com.eventhub.dao.repository.alloydb;

import com.eventhub.model.Integration;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface IntegrationRepository extends ListCrudRepository<Integration, Long> {
    @Query("SELECT d FROM Integration d WHERE d.organization.id = :orgId and d.workspace.id=:workspaceId")
    List<Integration> getIntegrations(Long orgId, Long workspaceId);

    @Query("SELECT d FROM Integration d WHERE d.organization.id = :orgId and d.workspace.id=:workspaceId and d.source.id=:sourceId")
    List<Integration> getIntegration(Long orgId, Long workspaceId, Long sourceId);
}
