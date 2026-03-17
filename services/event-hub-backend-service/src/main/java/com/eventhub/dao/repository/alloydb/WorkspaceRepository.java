package com.eventhub.dao.repository.alloydb;

import com.eventhub.model.Workspace;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkspaceRepository extends ListCrudRepository<Workspace, Long> {
    @Query("SELECT d FROM Workspace d WHERE d.organization.id = :orgId")
    List<Workspace> getWorkspaces(@Param("orgId") String orgId);
}
