package com.eventhub.dao.repository.alloydb;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;

import com.eventhub.model.Target;
import org.springframework.stereotype.Repository;

@Repository
public interface TargetRepository extends ListCrudRepository<Target, Long> {
	@Query("SELECT d FROM Target d WHERE d.organization.id = :orgId and d.workspace.id=:workspaceId")
	List<Target> getTargets(Long orgId, String workspaceId);
}
