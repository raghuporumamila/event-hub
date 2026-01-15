package com.eventhub.dao.repository.alloydb;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import com.eventhub.model.Source;
import org.springframework.stereotype.Repository;

@Repository
public interface SourceRepository extends ListCrudRepository<Source, Long> {

	@Query("SELECT d FROM Source d WHERE d.orgId = :orgId and d.workspace = :workspace")
	List<Source> findByOrgId(@Param("orgId") String orgId, @Param("workspace") String workspace);
}
